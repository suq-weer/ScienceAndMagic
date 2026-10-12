// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * 木种的「声明式定义」——只描述数据，不包含任何注册逻辑。
 * <p>
 * 新增一种木头时，通常只需要新增一个 {@code WoodMaterial} 常量，然后交给
 * {@link WoodSet#register} 即可完成全部方块/物品/实体的注册。
 * <p>
 * <b>贴图策略</b>：所有像素都来自某套<b>原版</b>木质贴图（默认橡木，由 {@link #baseNamespace()} /
 * {@link #basePrefix()} 指定），再由 {@link #palette()} 里的 OKLab 映射重着色成木种自己的贴图。
 * 方块/物品贴图在客户端装配图集时由自定义 sprite source 动态生成（不占用额外 PNG）；
 * 实体贴图与告示牌编辑界面贴图无法进入图集，由数据生成阶段烘焙。
 *
 * @param namespace        命名空间，例如 {@code science_and_magic}
 * @param name             木种名，例如 {@code sequoia}
 * @param zhName           中文显示名，例如 {@code 红杉木}（用于生成「红杉木原木」等文案）
 * @param baseNamespace    基础贴图所在命名空间，例如 {@code minecraft}
 * @param basePrefix       基础贴图前缀，例如 {@code oak}（对应 {@code oak_log} / {@code oak_planks} / {@code stripped_oak_log}）
 * @param palette          各类贴图的 OKLab 重着色参数
 * @param treePreset       树形预设 id（对应 {@code src/main/resources/gen/tree_shapes/} 下的目录名；运行时经
 *                         {@link TreePresetCatalog} 反查），决定「几 x 几 的树苗矩阵分别长成哪些形状」
 * @param biomeTags        自然生成的群系标签（可多个；<b>空表示不自然生成</b>，只保留树苗/骨粉催熟）
 * @param hasBoat          是否生成船/箱船
 * @param inCreativeTab    是否放入模组自定义创造模式物品栏
 */
public record WoodMaterial(
        String namespace,
        String name,
        String zhName,
        String baseNamespace,
        String basePrefix,
        WoodPalette palette,
        Identifier treePreset,
        List<TagKey<Biome>> biomeTags,
        boolean hasBoat,
        boolean inCreativeTab) {

    public WoodMaterial {
        biomeTags = List.copyOf(biomeTags);
    }

    /**
     * 是否参与自然生成。
     * <p>
     * {@link #biomeTags()} 为空时不会注册 biome modifier —— 该木种不会自然出现，
     * 但仍可通过树苗生长 / 骨粉催熟获得。
     */
    public boolean generatesNaturally() {
        return !this.biomeTags.isEmpty();
    }

    /** {@code namespace:name} */
    public Identifier id() {
        return Identifier.fromNamespaceAndPath(this.namespace, this.name);
    }

    /** {@code namespace:name + suffix}，例如 {@code science_and_magic:sequoia_log} */
    public Identifier id(String suffix) {
        return Identifier.fromNamespaceAndPath(this.namespace, this.name + suffix);
    }

    // ------------------------------------------------------------------
    // 贴图 / 模型路径
    // ------------------------------------------------------------------

    /** 某类贴图在本木种下的 sprite ID（图集贴图），例如 {@code science_and_magic:block/wood/sequoia/planks}。 */
    public Identifier sprite(WoodTexture texture) {
        return texture.spriteId(this.namespace, this.name);
    }

    /** 同 {@link #sprite(WoodTexture)} 的字符串形式，便于写进 JSON 与日志。 */
    public String spritePath(WoodTexture texture) {
        return this.namespace + ":" + texture.spriteId(this.namespace, this.name).getPath();
    }

    /** 某类非图集贴图的烘焙产物路径（相对 {@code assets/<ns>/}）。 */
    public String bakedPath(WoodTexture texture) {
        return texture.bakedPath(this.name);
    }

    /** 烘焙产物写入的命名空间（告示牌 GUI 贴图必然是 {@code minecraft}）。 */
    public String bakedNamespace(WoodTexture texture) {
        return texture.bakedNamespace(this.namespace);
    }

    /**
     * 本木种的方块模型 ID，例如 {@code science_and_magic:block/wood/sequoia/stairs}。
     * <p>
     * 每个木种拥有自己的薄模型：{@code parent} 指向原版几何模板（{@code minecraft:block/stairs} 等），
     * {@code textures} 指向本木种重着色后的 sprite。因为不再使用 {@code tintindex}，
     * 这些模型不能用一份共享模板代替。
     */
    public Identifier woodModel(String model) {
        return Identifier.fromNamespaceAndPath(this.namespace, "block/wood/" + this.name + "/" + model);
    }

    /** 本木种的物品模型 ID，例如 {@code science_and_magic:item/wood/sequoia/boat}。 */
    public Identifier itemModel(String model) {
        return Identifier.fromNamespaceAndPath(this.namespace, "item/wood/" + this.name + "/" + model);
    }

    /**
     * 船/箱船的模型层路径，例如 {@code boat/sequoia_boat}。
     * <p>
     * 26.2 的 {@code AbstractBoatRenderer} 会用「模型层路径」推导贴图路径
     * （{@code textures/entity/<层路径>.png}），因此同一个字符串既用于
     * {@code ModelLayerLocation}，也用于烘焙产物的文件名。
     *
     * @param kind {@code boat} 或 {@code chest_boat}
     */
    public String boatPath(String kind) {
        return "boat/" + this.name + "_" + kind;
    }

    /**
     * 便捷构造：使用原版橡木贴图 + 给定的调色板，默认在 {@code #minecraft:is_taiga}（针叶林类）自然生成。
     */
    public static WoodMaterial of(String namespace, String name, String zhName, WoodPalette palette,
            Identifier treePreset, boolean hasBoat, boolean inCreativeTab) {
        return of(namespace, name, zhName, palette, treePreset, List.of(BiomeTags.IS_TAIGA), hasBoat,
                inCreativeTab);
    }

    /**
     * 便捷构造（可自定义自然生成的群系标签）。
     * <p>
     * 传入空的 {@code biomeTags} 表示「不自然生成」；传入多个标签时，会为每个标签各生成一个
     * {@code neoforge:add_features} biome modifier。
     * <p>
     * 注意：标签必须真实存在，否则数据生成阶段会直接抛错（比静默不生成更容易发现）。
     */
    public static WoodMaterial of(String namespace, String name, String zhName, WoodPalette palette,
            Identifier treePreset, List<TagKey<Biome>> biomeTags, boolean hasBoat, boolean inCreativeTab) {
        return new WoodMaterial(namespace, name, zhName, "minecraft", "oak", palette, treePreset, biomeTags,
                hasBoat, inCreativeTab);
    }
}
