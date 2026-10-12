// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.level.material.MapColor;

import team.xcmt.sam.util.OkLab;
import team.xcmt.sam.util.WoodMapColors;
import team.xcmt.sam.util.OkLabMap;
import team.xcmt.sam.util.RecolorStats;

/**
 * 一个木种的调色板：<b>一个 sRGB 目标色</b>即可描述整套木头的颜色。
 * <p>
 * 重着色参数不写死常量，而是在运行时从贴图自身推导：
 * <ol>
 *     <li>取锚点类别（{@link WoodTexture#PLANKS}）的<b>源贴图均值</b>与该木种的目标色；</li>
 *     <li>用 OKLab 仿射把「源均值」精确对齐到「目标色」，同时保持源贴图的明暗层次
 *         （见 {@link OkLabMap#matching}）；</li>
 *     <li>其余类别复用同一个映射——这样木板与原木之间原有的明暗差异被完整保留，
 *         不会因为「各自对齐到同一亮度」而被压平。</li>
 * </ol>
 * <p>
 * 灰度源贴图（树叶）不需要特殊分支：其 {@code a}/{@code b} 均值恒为 0，
 * 仿射映射会自动注入目标色相。但树叶通常希望是绿色而不是木材色，因此提供了
 * {@link Builder#color(WoodTexture, int)} 按类别覆盖目标色。
 * <p>
 * <b>地图颜色</b>（{@link MapColor}）不需要声明：{@link #resolveMapColor} 会把每类方块的目标色
 * （类别覆盖优先，否则主色）折算成 OKLab 空间下最接近的原版地图色
 * ——「贴图是什么色，地图上就是什么色」。
 * <p>
 * 之所以只能折算、不能新建：原版地图色<b>只有 64 个</b>（{@code MapColor} 构造器 private，
 * 且地图存档是「每像素 1 字节 = 6 位色号 + 2 位明暗」，色号无法扩容；其中 62 个已被原版占用）。
 * 所以折算误差（例如偏暖的橙棕落到 {@code MapColor.WOOD}＝原版橡木色）是这套系统的固有边界，
 * 不是 bug。
 * <p>
 * 不可变、线程安全（{@code targetLab} 等派生值在构造时算好）。
 */
public final class WoodPalette {

    /** 默认明度对比度：1 表示完整保留源贴图的明暗层次。 */
    public static final float DEFAULT_LIGHTNESS_CONTRAST = 1.0F;

    /** 默认色度缩放：1 表示完整保留源贴图的饱和度起伏。 */
    public static final float DEFAULT_CHROMA_SCALE = 1.0F;

    private final int targetColor;
    private final float lightnessContrast;
    private final float chromaScale;
    private final Map<WoodTexture, Integer> colorOverrides;
    private final float[] targetLab;
    private final Map<WoodTexture, float[]> overrideLab;

    private WoodPalette(int targetColor, float lightnessContrast, float chromaScale,
            Map<WoodTexture, Integer> colorOverrides) {
        this.targetColor = targetColor;
        this.lightnessContrast = lightnessContrast;
        this.chromaScale = chromaScale;
        this.colorOverrides = Collections.unmodifiableMap(new EnumMap<>(colorOverrides));

        float[] mainLab = new float[3];
        OkLab.fromArgb(targetColor, mainLab);
        this.targetLab = mainLab;

        Map<WoodTexture, float[]> override = new EnumMap<>(WoodTexture.class);
        for (Map.Entry<WoodTexture, Integer> entry : colorOverrides.entrySet()) {
            float[] lab = new float[3];
            OkLab.fromArgb(entry.getValue(), lab);
            override.put(entry.getKey(), lab);
        }
        this.overrideLab = Collections.unmodifiableMap(override);
    }

    // ------------------------------------------------------------------
    // 参数
    // ------------------------------------------------------------------

    /** 木种主目标色（ARGB）。 */
    public int targetColor() {
        return this.targetColor;
    }

    /** 某类别的目标色；未覆盖时即主目标色。 */
    public int targetColor(WoodTexture texture) {
        return this.colorOverrides.getOrDefault(texture, this.targetColor);
    }

    public boolean hasOverride(WoodTexture texture) {
        return this.colorOverrides.containsKey(texture);
    }

    public Map<WoodTexture, Integer> colorOverrides() {
        return this.colorOverrides;
    }

    /**
     * 该类别的方块使用的地图颜色：把该类别的目标色折算成 OKLab 空间下最接近的原版
     * {@link MapColor}（见 {@link WoodMapColors}）。
     * <p>
     * {@code WoodSet} 注册方块与诊断日志都走这一个入口，因此「地图上到底是什么色」只有一处实现。
     */
    public MapColor resolveMapColor(WoodTexture texture) {
        return WoodMapColors.nearest(this.targetColor(texture));
    }

    /**
     * 诊断用文本：{@code 该类别的目标色→折算出的地图色}，例如 {@code #FF48A960→#58(#14B485)}。
     * <p>
     * 刻意把<b>输入色</b>也打出来：只打结果时，两个不同的目标色恰好折算到同一个原版色，
     * 会被误读成「没有按类别分别折算」。带上输入才能一眼看出每个类别用的是自己的色。
     */
    private String describeMapColor(WoodTexture texture) {
        return String.format("#%08X→%s", this.targetColor(texture),
                WoodMapColors.describe(resolveMapColor(texture)));
    }

    /**
     * 诊断用：一行列出「主色 → 地图色」，以及所有<b>目标色与主色不同</b>的类别。
     * <p>
     * 没列出别的类别是因为它们的目标色等于主色，必然得到同一个结果，列出来只是噪声。
     * 每项都带目标色，这样一眼能看出「每个类别各自用自己的色去找最邻近色」，
     * 而不是误以为全部套用了主色。
     */
    public String describeMapColors() {
        StringBuilder builder = new StringBuilder("主色(木板) ").append(describeMapColor(WoodTexture.PLANKS));
        for (WoodTexture texture : WoodTexture.atlasValues()) {
            if (texture != WoodTexture.PLANKS && this.targetColor(texture) != this.targetColor) {
                builder.append("｜").append(texture.key()).append(' ').append(describeMapColor(texture));
            }
        }
        return builder.toString();
    }

    public float lightnessContrast() {
        return this.lightnessContrast;
    }

    public float chromaScale() {
        return this.chromaScale;
    }

    // ------------------------------------------------------------------
    // 映射推导
    // ------------------------------------------------------------------

    /**
     * 计算锚点类别的映射：把 {@code anchorStats}（锚点源贴图，默认木板）的均值对齐到主目标色。
     * <p>
     * 每个木种只需算一次，随后交给 {@link #resolve} 复用。
     *
     * @param anchorStats 锚点类别源贴图的 OKLab 统计；为空统计时返回 {@link OkLabMap#IDENTITY}
     */
    public OkLabMap anchor(RecolorStats anchorStats) {
        if (anchorStats == null || anchorStats.isEmpty()) {
            return OkLabMap.IDENTITY;
        }
        return OkLabMap.matching(
                this.targetLab[0], this.targetLab[1], this.targetLab[2],
                anchorStats.meanL(), anchorStats.meanA(), anchorStats.meanB(),
                this.lightnessContrast, this.chromaScale);
    }

    /**
     * 取某类别的最终映射。
     * <p>
     * 未覆盖的类别直接复用 {@code anchorMap}（保证同一木种内所有贴图整体偏移一致）；
     * 被覆盖的类别用<b>自身</b>源贴图均值与覆盖色重新推导，从而在保留该贴图明暗层次的前提下换成目标色相。
     *
     * @param ownStats  该类别的源贴图统计（仅覆盖类别需要）
     * @param anchorMap {@link #anchor(RecolorStats)} 的结果
     */
    public OkLabMap resolve(WoodTexture texture, RecolorStats ownStats, OkLabMap anchorMap) {
        float[] override = this.overrideLab.get(texture);
        if (override == null) {
            return anchorMap;
        }
        if (ownStats == null || ownStats.isEmpty()) {
            return anchorMap;
        }
        return OkLabMap.matching(
                override[0], override[1], override[2],
                ownStats.meanL(), ownStats.meanA(), ownStats.meanB(),
                this.lightnessContrast, this.chromaScale);
    }

    /** 便捷方法：未覆盖类别也可用（此时 {@code ownStats} 会被忽略）。 */
    public OkLabMap resolve(WoodTexture texture, RecolorStats ownStats, RecolorStats anchorStats) {
        return resolve(texture, ownStats, anchor(anchorStats));
    }

    // ------------------------------------------------------------------
    // 构建
    // ------------------------------------------------------------------

    public static Builder builder(int targetColor) {
        return new Builder(targetColor);
    }

    @Override
    public String toString() {
        return String.format("WoodPalette[target=#%08X, contrast=%.3f, chroma=%.3f, overrides=%d]",
                this.targetColor, this.lightnessContrast, this.chromaScale, this.colorOverrides.size());
    }

    /** 调色板构建器。 */
    public static final class Builder {

        private final int targetColor;
        private final Map<WoodTexture, Integer> colorOverrides = new EnumMap<>(WoodTexture.class);
        private float lightnessContrast = DEFAULT_LIGHTNESS_CONTRAST;
        private float chromaScale = DEFAULT_CHROMA_SCALE;

        private Builder(int targetColor) {
            this.targetColor = targetColor;
        }

        /**
         * 明度对比度缩放。
         * <p>
         * 1.0 表示完整保留源贴图的明暗层次；介于 0 与 1 之间会向目标亮度「压平」；
         * 大于 1 会增大对比。不建议超过 1.2——过度提亮会丢失高光细节。
         */
        public Builder lightnessContrast(float value) {
            if (value <= 0.0F) {
                throw new IllegalArgumentException("lightnessContrast 必须为正数，收到 " + value);
            }
            this.lightnessContrast = value;
            return this;
        }

        /** 色度缩放；0 表示完全按目标色的饱和度输出（丢弃源贴图的饱和度起伏）。 */
        public Builder chromaScale(float value) {
            if (value < 0.0F) {
                throw new IllegalArgumentException("chromaScale 不能为负，收到 " + value);
            }
            this.chromaScale = value;
            return this;
        }

        /** 为某类别单独指定目标色。 */
        public Builder color(WoodTexture texture, int argb) {
            this.colorOverrides.put(texture, argb);
            return this;
        }

        /** 树叶目标色。树叶源贴图是灰度图，不覆盖就会变成木材色。 */
        public Builder leaves(int argb) {
            return color(WoodTexture.LEAVES, argb);
        }

        public WoodPalette build() {
            return new WoodPalette(this.targetColor, this.lightnessContrast, this.chromaScale,
                    this.colorOverrides);
        }
    }
}
