// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;

/**
 * 木种贴图类别表——「一张贴图从哪来、到哪去」的唯一事实来源。
 * <p>
 * 框架只使用<b>原版基础贴图</b>作为像素来源（默认橡木，由 {@link WoodMaterial#baseNamespace()} /
 * {@link WoodMaterial#basePrefix()} 决定），在运行或数据生成阶段用
 * {@link team.xcmt.sam.util.OkLabMap} 重着色成木种自己的贴图。因此这里只声明
 * 「来源路径模板」与「目标路径模板」，不包含任何颜色数值。
 * <p>
 * 路径模板占位符：
 * <ul>
 *     <li>{@code %s} —— 在<b>来源</b>模板中表示原版基础前缀（如 {@code oak}）；
 *         在<b>目标</b>模板中表示木种名（如 {@code sequoia}）</li>
 * </ul>
 * <p>
 * 三类去向（{@link Target}）：
 * <ul>
 *     <li>{@link Target#BLOCK_ATLAS} / {@link Target#ITEM_ATLAS} —— 由运行时的自定义 sprite source
 *         在装配图集时动态重着色，<b>不会产生任何 PNG 文件</b></li>
 *     <li>{@link Target#BAKED_MOD} / {@link Target#BAKED_MINECRAFT} —— 无法进入图集
 *         （实体贴图由 {@code ModelLayerLocation} 推导；告示牌编辑界面贴图由 {@code WoodType.name()} 硬拼），
 *         必须由数据生成阶段烘焙成实体 PNG</li>
 * </ul>
 */
public enum WoodTexture {

    // ---- 方块图集 ----
    /** 木板贴图；楼梯/台阶/栅栏/栅栏门/按钮/压力板等派生方块按原版做法全部复用它。 */
    PLANKS("planks", Target.BLOCK_ATLAS, "block/%s_planks", "block/wood/%s/planks", false),
    /** 原木侧面。 */
    LOG("log", Target.BLOCK_ATLAS, "block/%s_log", "block/wood/%s/log", false),
    /** 原木顶面（年轮）。 */
    LOG_TOP("log_top", Target.BLOCK_ATLAS, "block/%s_log_top", "block/wood/%s/log_top", false),
    /** 去皮原木侧面。 */
    STRIPPED_LOG("stripped_log", Target.BLOCK_ATLAS, "block/stripped_%s_log", "block/wood/%s/stripped_log", false),
    /** 去皮原木顶面。 */
    STRIPPED_LOG_TOP("stripped_log_top", Target.BLOCK_ATLAS, "block/stripped_%s_log_top",
            "block/wood/%s/stripped_log_top", false),
    /** 门下半部分。 */
    DOOR_BOTTOM("door_bottom", Target.BLOCK_ATLAS, "block/%s_door_bottom", "block/wood/%s/door_bottom", false),
    /** 门上半部分。 */
    DOOR_TOP("door_top", Target.BLOCK_ATLAS, "block/%s_door_top", "block/wood/%s/door_top", false),
    /** 活板门。 */
    TRAPDOOR("trapdoor", Target.BLOCK_ATLAS, "block/%s_trapdoor", "block/wood/%s/trapdoor", false),
    /** 货架（26.2 新增方块）。 */
    SHELF("shelf", Target.BLOCK_ATLAS, "block/%s_shelf", "block/wood/%s/shelf", false),
    /** 告示牌木板。 */
    SIGN("sign", Target.BLOCK_ATLAS, "block/%s_sign", "block/wood/%s/sign", false),
    /** 悬挂告示牌木板。 */
    HANGING_SIGN("hanging_sign", Target.BLOCK_ATLAS, "block/%s_hanging_sign", "block/wood/%s/hanging_sign", false),
    /**
     * 树叶。
     * <p>
     * 原版只有橡木/白桦/丛林/金合欢的树叶贴图是<b>灰度图</b>（其余靠生物群系叶色染色），
     * 因此本类别标记为灰度：源的 {@code a}/{@code b} 恒为 0，需要用
     * {@link team.xcmt.sam.util.OkLabMap#matching} 直接注入目标色度。
     */
    LEAVES("leaves", Target.BLOCK_ATLAS, "block/%s_leaves", "block/wood/%s/leaves", true),
    /** 树苗（原版即为彩色贴图）。 */
    SAPLING("sapling", Target.BLOCK_ATLAS, "block/%s_sapling", "block/wood/%s/sapling", false),

    // ---- 物品图集 ----
    /** 门的物品图标。 */
    ITEM_DOOR("item_door", Target.ITEM_ATLAS, "item/%s_door", "item/wood/%s/door", false),
    /** 告示牌的物品图标。 */
    ITEM_SIGN("item_sign", Target.ITEM_ATLAS, "item/%s_sign", "item/wood/%s/sign", false),
    /** 悬挂告示牌的物品图标。 */
    ITEM_HANGING_SIGN("item_hanging_sign", Target.ITEM_ATLAS, "item/%s_hanging_sign",
            "item/wood/%s/hanging_sign", false),
    /** 船的物品图标。 */
    ITEM_BOAT("item_boat", Target.ITEM_ATLAS, "item/%s_boat", "item/wood/%s/boat", false),
    /** 箱船的物品图标。 */
    ITEM_CHEST_BOAT("item_chest_boat", Target.ITEM_ATLAS, "item/%s_chest_boat", "item/wood/%s/chest_boat", false),

    // ---- 非图集（必须烘焙成 PNG）----
    /** 船实体贴图；路径与 {@code ModelLayerLocation} 推导出的贴图路径一致。 */
    ENTITY_BOAT("entity_boat", Target.BAKED_MOD, "entity/boat/%s", "textures/entity/boat/%s_boat.png", false, true),
    /** 箱船实体贴图。 */
    ENTITY_CHEST_BOAT("entity_chest_boat", Target.BAKED_MOD, "entity/chest_boat/%s",
            "textures/entity/boat/%s_chest_boat.png", false, true),
    /**
     * 告示牌编辑界面贴图。
     * <p>
     * {@code SignEditScreen} 用 {@code Identifier.parse(woodType.name() + ".png")} 拼路径，
     * 命名空间被 {@code Identifier.parse} 固定为 {@code minecraft}，所以只能写到
     * {@code assets/minecraft/textures/gui/signs/<木种名>.png}。
     */
    GUI_SIGN("gui_sign", Target.BAKED_MINECRAFT, "gui/signs/%s", "textures/gui/signs/%s.png", false),
    /** 悬挂告示牌编辑界面贴图；同样受限于 {@code minecraft} 命名空间。 */
    GUI_HANGING_SIGN("gui_hanging_sign", Target.BAKED_MINECRAFT, "gui/hanging_signs/%s",
            "textures/gui/hanging_signs/%s.png", false);

    /** {@link #key()} 到枚举的反查表；图集 JSON 与烘焙器都只携带字符串键。 */
    private static final Map<String, WoodTexture> BY_KEY = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(WoodTexture::key, Function.identity()));

    /** 按 {@link #key()} 反查类别；键不存在时返回 {@code null}。 */
    public static WoodTexture byKey(String key) {
        return BY_KEY.get(key);
    }

    /** 贴图去向。 */
    public enum Target {
        /** 进入 {@code minecraft:blocks} 图集，运行时动态重着色。 */
        BLOCK_ATLAS("minecraft:blocks", true),
        /** 进入 {@code minecraft:items} 图集，运行时动态重着色。 */
        ITEM_ATLAS("minecraft:items", true),
        /** 烘焙到本模组命名空间的 {@code assets/<ns>/<path>}。 */
        BAKED_MOD(null, false),
        /** 烘焙到 {@code minecraft} 命名空间（跨命名空间写资源，仅告示牌 GUI 需要）。 */
        BAKED_MINECRAFT("minecraft", false);

        private final String atlasId;
        private final boolean atlas;

        Target(String atlasId, boolean atlas) {
            this.atlasId = atlasId;
            this.atlas = atlas;
        }

        /** 是否由运行时 sprite source 动态生成（否则需要烘焙）。 */
        public boolean isAtlas() {
            return this.atlas;
        }

        /** 图集 ID，例如 {@code minecraft:blocks}；非图集返回空串。 */
        public String atlasId() {
            return this.atlasId;
        }

        /** 烘焙产物写入的命名空间；非烘焙目标返回 {@code null}。 */
        public String bakedNamespace() {
            return this.atlas ? null : this.atlasId;
        }
    }

    private final String key;
    private final Target target;
    private final String sourceTemplate;
    private final String targetTemplate;
    private final boolean grayscaleSource;
    private final boolean requiresBoat;

    WoodTexture(String key, Target target, String sourceTemplate, String targetTemplate, boolean grayscaleSource) {
        this(key, target, sourceTemplate, targetTemplate, grayscaleSource, false);
    }

    WoodTexture(String key, Target target, String sourceTemplate, String targetTemplate, boolean grayscaleSource,
            boolean requiresBoat) {
        this.key = key;
        this.target = target;
        this.sourceTemplate = sourceTemplate;
        this.targetTemplate = targetTemplate;
        this.grayscaleSource = grayscaleSource;
        this.requiresBoat = requiresBoat;
    }

    /** 类别键，用于调色板查找与图集 JSON 的 {@code category} 字段。 */
    public String key() {
        return this.key;
    }

    public Target target() {
        return this.target;
    }

    /** 源贴图是否为灰度图（决定色度是「相对偏移」还是「绝对注入」）。 */
    public boolean grayscaleSource() {
        return this.grayscaleSource;
    }

    /** 是否只在木种启用船时才有意义（船 / 箱船实体贴图）。 */
    public boolean requiresBoat() {
        return this.requiresBoat;
    }

    /**
     * 原版源贴图的路径（不含 {@code textures/} 前缀与 {@code .png} 后缀），
     * 例如 {@code block/oak_planks}、{@code entity/boat/oak}。
     *
     * @param basePrefix 原版基础前缀，例如 {@code oak}
     */
    public String sourcePath(String basePrefix) {
        return this.sourceTemplate.formatted(basePrefix);
    }

    /**
     * 原版源贴图的完整 ID（命名空间 + 路径），例如 {@code minecraft:block/oak_planks}；
     * 图集 sprites 的 ID 与模型里的贴图引用都是这个形式。
     */
    public Identifier sourceId(String baseNamespace, String basePrefix) {
        return Identifier.fromNamespaceAndPath(baseNamespace, sourcePath(basePrefix));
    }

    /** 原版源贴图的资源文件 ID，例如 {@code minecraft:textures/block/oak_planks.png}。 */
    public Identifier sourceFile(String baseNamespace, String basePrefix) {
        return Identifier.fromNamespaceAndPath(baseNamespace, "textures/" + sourcePath(basePrefix) + ".png");
    }

    /**
     * 图集目标：本木种 sprite 的路径（不含 {@code textures/} 前缀与 {@code .png} 后缀），
     * 例如 {@code block/wood/sequoia/planks}。模型里的 {@code textures} 就引用它。
     *
     * @param namespace 木种命名空间
     * @param woodName  木种名，例如 {@code sequoia}
     */
    public Identifier spriteId(String namespace, String woodName) {
        if (!this.target.isAtlas()) {
            throw new IllegalStateException(this.key + " 不是图集贴图（target=" + this.target + "）");
        }
        return Identifier.fromNamespaceAndPath(namespace, this.targetTemplate.formatted(woodName));
    }

    /**
     * 烘焙目标：相对 {@code assets/<ns>/} 的文件路径，例如
     * {@code textures/entity/boat/sequoia_boat.png}、{@code textures/gui/signs/sequoia.png}。
     */
    public String bakedPath(String woodName) {
        if (this.target.isAtlas()) {
            throw new IllegalStateException(this.key + " 是图集贴图，不需要烘焙（target=" + this.target + "）");
        }
        return this.targetTemplate.formatted(woodName);
    }

    /** 烘焙产物所在命名空间：{@code minecraft} 或木种自己的命名空间。 */
    public String bakedNamespace(String woodNamespace) {
        String fixed = this.target.bakedNamespace();
        return fixed != null ? fixed : woodNamespace;
    }

    /** 仅图集类别（由运行时 sprite source 动态重着色）。 */
    public static WoodTexture[] atlasValues() {
        return Arrays.stream(values()).filter(t -> t.target.isAtlas()).toArray(WoodTexture[]::new);
    }

    /** 仅烘焙类别（需要产出 PNG 文件）。 */
    public static WoodTexture[] bakedValues() {
        return Arrays.stream(values()).filter(t -> !t.target.isAtlas()).toArray(WoodTexture[]::new);
    }
}
