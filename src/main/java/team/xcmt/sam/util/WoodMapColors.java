// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.level.material.MapColor;

/**
 * 把任意 sRGB 颜色折算成<b>最接近的原版 {@link MapColor}</b>：在 OKLab 空间里取距离最小的那个。
 * <p>
 * <b>为什么需要折算而不是直接新建 MapColor</b>：26.2 的 {@code MapColor(int, int)} 构造器是 private，
 * 且地图存档/网络都是「每像素 1 字节 = 6 位色号 + 2 位明暗」（{@code MapItemSavedData.colors} 是
 * {@code byte[16384]}，{@code getPackedId} 只留 6 位给色号），所以全游戏只有 64 个色号、
 * 其中 62 个已被原版占用。模组无法扩容，只能在原版公开常量里挑。
 * <p>
 * <b>为什么用 OKLab 而不是 RGB 欧氏距离</b>：OKLab 的设计目标就是「距离≈感知差异」，
 * 因此明暗与色相的权重更接近肉眼。RGB 距离会把「深紫黑」误判成饱和紫色，而 OKLab 会把它
 * 落到 {@code COLOR_BLACK} / {@code TERRACOTTA_BLACK} 一带。这里与贴图重着色用的是同一套色彩空间，
 * 所以「贴图看起来是什么色」与「地图上是什么色」的判断标准一致。
 */
public final class WoodMapColors {

    /**
     * 候选：原版全部公开常量。
     * <p>
     * 刻意排除 {@link MapColor#NONE}（id 0）——它表示「地图上透明」，不是一种颜色。
     */
    private static final List<MapColor> CANDIDATES = List.of(
            // 材质色
            MapColor.GRASS, MapColor.SAND, MapColor.WOOL, MapColor.FIRE, MapColor.ICE, MapColor.METAL,
            MapColor.PLANT, MapColor.SNOW, MapColor.CLAY, MapColor.DIRT, MapColor.STONE, MapColor.WATER,
            MapColor.WOOD, MapColor.QUARTZ,
            // 16 种染料色
            MapColor.COLOR_ORANGE, MapColor.COLOR_MAGENTA, MapColor.COLOR_LIGHT_BLUE, MapColor.COLOR_YELLOW,
            MapColor.COLOR_LIGHT_GREEN, MapColor.COLOR_PINK, MapColor.COLOR_GRAY, MapColor.COLOR_LIGHT_GRAY,
            MapColor.COLOR_CYAN, MapColor.COLOR_PURPLE, MapColor.COLOR_BLUE, MapColor.COLOR_BROWN,
            MapColor.COLOR_GREEN, MapColor.COLOR_RED, MapColor.COLOR_BLACK,
            // 矿物 / 特殊
            MapColor.GOLD, MapColor.DIAMOND, MapColor.LAPIS, MapColor.EMERALD, MapColor.PODZOL, MapColor.NETHER,
            // 16 种带釉陶瓦色
            MapColor.TERRACOTTA_WHITE, MapColor.TERRACOTTA_ORANGE, MapColor.TERRACOTTA_MAGENTA,
            MapColor.TERRACOTTA_LIGHT_BLUE, MapColor.TERRACOTTA_YELLOW, MapColor.TERRACOTTA_LIGHT_GREEN,
            MapColor.TERRACOTTA_PINK, MapColor.TERRACOTTA_GRAY, MapColor.TERRACOTTA_LIGHT_GRAY,
            MapColor.TERRACOTTA_CYAN, MapColor.TERRACOTTA_PURPLE, MapColor.TERRACOTTA_BLUE,
            MapColor.TERRACOTTA_BROWN, MapColor.TERRACOTTA_GREEN, MapColor.TERRACOTTA_RED,
            MapColor.TERRACOTTA_BLACK,
            // 下界
            MapColor.CRIMSON_NYLIUM, MapColor.CRIMSON_STEM, MapColor.CRIMSON_HYPHAE, MapColor.WARPED_NYLIUM,
            MapColor.WARPED_STEM, MapColor.WARPED_HYPHAE, MapColor.WARPED_WART_BLOCK,
            // 其他
            MapColor.DEEPSLATE, MapColor.RAW_IRON, MapColor.GLOW_LICHEN);

    /** 候选色的 OKLab 坐标（构造期算一次，与 {@link #CANDIDATES} 一一对应）。 */
    private static final float[][] CANDIDATE_LAB = labOf(CANDIDATES);

    /** 已折算过的颜色；同一颜色只算一次。 */
    private static final Map<Integer, MapColor> CACHE = new ConcurrentHashMap<>();

    private WoodMapColors() {
    }

    /**
     * 取与 {@code argb} 最接近的原版 {@link MapColor}。
     * <p>
     * 距离是 OKLab 空间的欧氏距离平方。并列时取 {@link #CANDIDATES} 中靠前的那个，
     * 因此结果完全确定：同一个颜色永远得到同一个色号。
     *
     * @param argb 任意 sRGB 颜色（忽略 alpha）
     */
    public static MapColor nearest(int argb) {
        MapColor cached = CACHE.get(argb);
        if (cached != null) {
            return cached;
        }

        float[] target = new float[3];
        OkLab.fromArgb(argb, target);

        int best = 0;
        float bestDistance = Float.MAX_VALUE;
        for (int i = 0; i < CANDIDATE_LAB.length; i++) {
            float[] lab = CANDIDATE_LAB[i];
            float dL = lab[0] - target[0];
            float dA = lab[1] - target[1];
            float dB = lab[2] - target[2];
            float distance = dL * dL + dA * dA + dB * dB;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }

        MapColor result = CANDIDATES.get(best);
        CACHE.put(argb, result);
        return result;
    }

    /**
     * 诊断用的可读文本：原版色号 + 该色号自己的 RGB，例如 {@code #13(#8F7648)}。
     * <p>
     * 折算结果肉眼没法从目标色推断，日志里带上色号与 RGB 才能一眼看出「落到了哪个原版色」。
     */
    public static String describe(MapColor mapColor) {
        return String.format("#%d(#%06X)", mapColor.id, mapColor.col);
    }

    private static float[][] labOf(List<MapColor> colors) {
        float[][] lab = new float[colors.size()][];
        for (int i = 0; i < colors.size(); i++) {
            float[] value = new float[3];
            // MapColor.col 是 24 位 RGB（无 alpha），补上不透明通道再转 OKLab。
            OkLab.fromArgb(0xFF000000 | colors.get(i).col, value);
            lab[i] = value;
        }
        return lab;
    }
}
