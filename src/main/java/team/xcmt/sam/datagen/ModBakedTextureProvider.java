// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import com.google.common.hash.Hashing;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodPalette;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.api.wood.WoodTexture;
import team.xcmt.sam.util.OkLabMap;
import team.xcmt.sam.util.RecolorStats;

/**
 * 「非图集贴图」的烘焙器：把那些无法进入图集、只能以独立 PNG 形式存在的木种贴图
 * 在数据生成阶段重着色后写出来。目前包括：
 * <ul>
 *     <li><b>船 / 箱船实体贴图</b> —— {@code AbstractBoatRenderer} 由 {@code ModelLayerLocation}
 *         推导 {@code textures/entity/<层路径>.png}，贴图不经图集；</li>
 *     <li><b>告示牌 / 悬挂告示牌编辑界面贴图</b> —— {@code SignEditScreen} 用
 *         {@code Identifier.parse(woodType.name() + ".png")} 拼路径，命名空间被固定为
 *         {@code minecraft}，所以只能写到 {@code assets/minecraft/textures/gui/...}。</li>
 * </ul>
 * 参数与运行时 sprite source 共用同一份 {@link WoodPalette} / {@link OkLabMap} 实现，
 * 两处颜色不会漂移。锚点（木板）的统计量也按同样的方式读取，保证整块木头偏移一致。
 */
public final class ModBakedTextureProvider implements DataProvider {

    private final Path resourcesFolder;
    private final ResourceManager resourceManager;

    public ModBakedTextureProvider(PackOutput output, ResourceManager resourceManager) {
        // createPathProvider 无法表达「写入 minecraft 命名空间」这类跨命名空间路径，
        // 这里直接用资源包根目录拼路径：assets/<命名空间>/<贴图路径>。
        this.resourcesFolder = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
        this.resourceManager = resourceManager;
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            WoodPalette palette = material.palette();
            OkLabMap anchor = palette.anchor(readAnchorStats(material));

            for (WoodTexture texture : WoodTexture.bakedValues()) {
                if (texture.requiresBoat() && !material.hasBoat()) {
                    continue;
                }
                futures.add(bake(cache, material, palette, texture, anchor));
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * 读取锚点类别（木板）源贴图的统计量。
     * <p>
     * 与运行时 sprite source 的取法完全一致：未显式覆盖目标色的类别都会套用由它推导出的
     * 同一个仿射映射，从而保留木板 / 原木 / 船之间的相对明暗。
     */
    private RecolorStats readAnchorStats(WoodMaterial material) {
        Identifier source = WoodTexture.PLANKS.sourceFile(material.baseNamespace(), material.basePrefix());
        try {
            BufferedImage image = read(source);
            return image == null ? RecolorStats.EMPTY : statsOf(image);
        } catch (IOException e) {
            ScienceAndMagic.LOGGER.warn("无法读取锚点贴图 {}，{} 的非图集贴图将不做重着色", source, material.name());
            return RecolorStats.EMPTY;
        }
    }

    private CompletableFuture<?> bake(CachedOutput cache, WoodMaterial material, WoodPalette palette,
            WoodTexture texture, OkLabMap anchor) {
        Identifier source = texture.sourceFile(material.baseNamespace(), material.basePrefix());
        Path target = this.resourcesFolder
                .resolve(material.bakedNamespace(texture))
                .resolve(material.bakedPath(texture));

        return CompletableFuture.runAsync(() -> {
            try {
                BufferedImage image = read(source);
                if (image == null) {
                    ScienceAndMagic.LOGGER.warn("缺少原版贴图 {}，跳过 {} 的 {} 烘焙", source, material.name(),
                            texture.key());
                    return;
                }
                OkLabMap map = palette.resolve(texture, statsOf(image), anchor);
                byte[] data = encodePng(recolor(image, map));
                // Hashing.sha1() 已过时；缓存只做「内容是否变化」的比较，用 sha256 等价且不过时。
                cache.writeIfNeeded(target, data, Hashing.sha256().hashBytes(data));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to bake texture " + target, e);
            }
        });
    }

    private BufferedImage read(Identifier source) throws IOException {
        Optional<Resource> resource = this.resourceManager.getResource(source);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream in = resource.get().open()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new IOException("Not a readable PNG: " + source);
            }
            return image;
        }
    }

    /** 逐像素应用 OKLab 映射（完全透明的像素由 {@link OkLabMap#applyArgb(int)} 原样跳过）。 */
    private static BufferedImage recolor(BufferedImage image, OkLabMap map) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out.setRGB(x, y, map.applyArgb(image.getRGB(x, y)));
            }
        }
        return out;
    }

    private static RecolorStats statsOf(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[index++] = image.getRGB(x, y);
            }
        }
        return RecolorStats.of(pixels);
    }

    private static byte[] encodePng(BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        return bytes.toByteArray();
    }

    @Override
    public @NonNull String getName() {
        return "Wood Baked Textures";
    }
}
