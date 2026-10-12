// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodPalette;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.api.wood.WoodTexture;
import team.xcmt.sam.util.OkLabMap;
import team.xcmt.sam.util.RecolorStats;

/**
 * 自定义图集源 {@code science_and_magic:oklab_recolor}：装配图集时把一张原版贴图
 * 按木种的 OKLab 参数<b>动态重着色</b>成木种自己的 sprite。
 * <p>
 * 相比把重着色结果预先烘焙成 PNG，这种做法只在客户端加载资源时多做几千次浮点运算，
 * 却完全不占额外的模组体积，而且改颜色只需要改注册处的目标色。
 * <p>
 * <b>为什么要自己加载源资源</b>：图集里 sprite 的身份来自
 * {@code SpriteContents.name()}（{@code Stitcher.Entry.name()}），而它由
 * {@code SpriteResourceLoader#loadSprite(id, resource)} 的实参决定；
 * {@code SpriteSourceList} 只把 loader 摊平成列表，会丢掉 {@code Output#add} 的 id。
 * 因此源贴图必须用<b>我们的目标 id</b> 加载，否则 sprite 会被登记成原版的 id
 * （既找不到自己的贴图，还会覆盖原版贴图）。
 * <p>
 * 工作方式：解析源资源后，用 {@link SpriteResourceLoader#loadSprite(Identifier, Resource)}
 * 以目标 id 生成 {@link SpriteContents}（保留 mcmeta），再把它的
 * {@link NativeImage} <b>原地</b>改写。之所以能原地改：
 * <ul>
 *     <li>{@code SpriteLoader} 是在所有 loader 取完图<b>之后</b>才调用 {@code increaseMipLevel}，
 *         所以 mipmap 会基于我们改过的像素重新生成，不需要手动重建；</li>
 *     <li>重着色不改变 alpha，{@code SpriteContents} 在构造时算出的 {@code Transparency} 依然有效，
 *         因此方块渲染层（cutout / translucent）也跟着正确；</li>
 *     <li>原地改不替换 {@code SpriteContents} 实例，{@code mcmeta}（动画、mipmap 策略）自然保留，
 *         也不会出现旧实例未释放的泄漏。</li>
 * </ul>
 * <p>
 * 参数不写在 JSON 里：JSON 只声明「哪个木种、哪个贴图类别、源贴图、目标 sprite」，
 * 数值由 {@link WoodMaterial#palette()} 现场用「源贴图 OKLab 均值 → 目标色」推导，
 * 保证与数据生成阶段烘焙实体贴图时用的是同一套逻辑。
 */
public record OklabRecolorSpriteSource(
        Identifier wood,
        String category,
        Identifier source,
        Identifier sprite) implements SpriteSource {

    /** 注册进 {@code SpriteSources} 的类型 ID。 */
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "oklab_recolor");

    public static final MapCodec<OklabRecolorSpriteSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Identifier.CODEC.fieldOf("wood").forGetter(OklabRecolorSpriteSource::wood),
                    Codec.STRING.fieldOf("category").forGetter(OklabRecolorSpriteSource::category),
                    Identifier.CODEC.fieldOf("source").forGetter(OklabRecolorSpriteSource::source),
                    Identifier.CODEC.fieldOf("sprite").forGetter(OklabRecolorSpriteSource::sprite))
            .apply(instance, OklabRecolorSpriteSource::new));

    /** 在 {@link RegisterSpriteSourcesEvent} 中调用，注册本类型。 */
    public static void register(RegisterSpriteSourcesEvent event) {
        event.register(ID, MAP_CODEC);
    }

    @Override
    public @NonNull MapCodec<? extends SpriteSource> codec() {
        return MAP_CODEC;
    }

    @Override
    public void run(@NonNull ResourceManager resourceManager, SpriteSource.@NonNull Output output) {
        run(resourceManager, output, Set.of());
    }

    @Override
    public void run(@NonNull ResourceManager resourceManager, SpriteSource.@NonNull Output output,
            @NonNull Set<MetadataSectionType<?>> additionalMetadata) {
        WoodSet set = WoodSet.byId(this.wood);
        WoodTexture texture = WoodTexture.byKey(this.category);
        if (set == null) {
            ScienceAndMagic.LOGGER.warn("图集源引用了未注册的木种 {}，跳过贴图 {}", this.wood, this.sprite);
            return;
        }
        if (texture == null) {
            ScienceAndMagic.LOGGER.warn("图集源引用了未知的贴图类别 {}，跳过贴图 {}", this.category, this.sprite);
            return;
        }

        // 源贴图按 SpriteSource 的约定解析：id -> textures/<path>.png
        Identifier sourceFile = TEXTURE_ID_CONVERTER.idToFile(this.source);
        Optional<Resource> resource = resourceManager.getResource(sourceFile);
        if (resource.isEmpty()) {
            ScienceAndMagic.LOGGER.warn("找不到源贴图 {}，跳过 {}", sourceFile, this.sprite);
            return;
        }

        WoodMaterial material = set.material();
        WoodPalette palette = material.palette();

        // 锚点（木板）的统计量：同一木种所有未覆盖目标色的类别共用同一个映射，
        // 从而保留木板 / 原木 / 门之间的相对明暗。
        RecolorStats anchorStats = readStats(resourceManager,
                WoodTexture.PLANKS.sourceFile(material.baseNamespace(), material.basePrefix()));
        OkLabMap anchor = palette.anchor(anchorStats == null ? RecolorStats.EMPTY : anchorStats);
        if (anchorStats == null) {
            ScienceAndMagic.LOGGER.warn("缺少锚点贴图（{}:block/{}_planks），{} 的贴图 {} 将保持原版外观",
                    material.baseNamespace(), material.basePrefix(), material.name(), this.sprite);
        }

        Resource sourceResource = resource.get();
        output.add(this.sprite, loader -> {
            SpriteContents contents = loader.loadSprite(this.sprite, sourceResource);
            NativeImage image = contents.getOriginalImage();
            OkLabMap map = palette.resolve(texture, statsOf(image), anchor);
            if (!map.isIdentity()) {
                applyInPlace(image, map);
            }
            return contents;
        });
    }

    /** 读取一张贴图并统计其 OKLab 均值；缺失或损坏时返回 {@code null}。 */
    private static RecolorStats readStats(ResourceManager resourceManager, Identifier file) {
        Optional<Resource> resource = resourceManager.getResource(file);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream in = resource.get().open();
                NativeImage image = NativeImage.read(in)) {
            return statsOf(image);
        } catch (IOException e) {
            ScienceAndMagic.LOGGER.warn("读取贴图 {} 失败，将不做重着色：{}", file, e.toString());
            return null;
        }
    }

    private static RecolorStats statsOf(NativeImage image) {
        return RecolorStats.of(image.getPixels());
    }

    /** 逐像素改写；完全透明的像素由 {@link OkLabMap#applyArgb(int)} 原样返回。 */
    private static void applyInPlace(NativeImage image, OkLabMap map) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getPixel(x, y);
                int recolored = map.applyArgb(argb);
                if (recolored != argb) {
                    image.setPixel(x, y, recolored);
                }
            }
        }
    }
}
