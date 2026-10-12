// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.util;

/**
 * 一张源贴图在 OKLab 空间中的统计量（按 alpha 加权均值）。
 * <p>
 * 重着色参数不写死常量，而是由「源贴图均值 → 目标色」的关系在现场推导：
 * 这样同一个目标色对不同明暗的贴图（木板 vs 原木侧面）会得到一致的整体偏移，
 * 保留贴图之间的相对层次，而不会把浅色贴图压暗、把深色贴图提亮到同一亮度。
 *
 * @param meanL       按 alpha 加权的平均亮度
 * @param meanA       按 alpha 加权的平均 a
 * @param meanB       按 alpha 加权的平均 b
 * @param sampleCount 参与统计的不透明像素数（alpha &gt; 0）
 */
public record RecolorStats(float meanL, float meanA, float meanB, int sampleCount) {

    /** 无有效像素时的空统计。 */
    public static final RecolorStats EMPTY = new RecolorStats(0.0F, 0.0F, 0.0F, 0);

    /**
     * 统计 {@code 0xAARRGGBB} 像素数组。
     * <p>
     * 权重取 {@code alpha / 255}：半透明边缘像素对整体色相的贡献应当小于实心像素；
     * 完全透明的像素直接跳过。
     */
    public static RecolorStats of(int[] argbPixels) {
        double weightSum = 0.0D;
        double lSum = 0.0D;
        double aSum = 0.0D;
        double bSum = 0.0D;
        int count = 0;

        float[] lab = new float[3];
        for (int argb : argbPixels) {
            int alpha = (argb >>> 24) & 0xFF;
            if (alpha == 0) {
                continue;
            }
            float weight = alpha / 255.0F;
            OkLab.fromArgb(argb, lab);
            lSum += weight * lab[0];
            aSum += weight * lab[1];
            bSum += weight * lab[2];
            weightSum += weight;
            count++;
        }

        if (weightSum <= 0.0D) {
            return EMPTY;
        }
        return new RecolorStats(
                (float) (lSum / weightSum),
                (float) (aSum / weightSum),
                (float) (bSum / weightSum),
                count);
    }

    public boolean isEmpty() {
        return this.sampleCount == 0;
    }
}
