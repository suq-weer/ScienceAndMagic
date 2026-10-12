package team.xcmt.sam.util;

/**
 * OKLab 亮度重着色映射——木种贴图染色的核心参数。
 * <p>
 * 对每个像素：
 * <ol>
 *     <li>sRGB 通道 → 线性 sRGB（去 gamma）</li>
 *     <li>线性 sRGB → OKLab {@code (L, a, b)}</li>
 *     <li>{@code L' = knee(lScale * L + lOffset)}；{@code a' = aScale * a + aOffset}；
 *         {@code b' = bScale * b + bOffset}</li>
 *     <li>OKLab → 线性 sRGB，越界时按比例降 chroma（不裁剪），再转回 sRGB</li>
 * </ol>
 * <p>
 * <b>为什么以 L 为主</b>：只缩放/平移 {@code L} 即可改变明度的同时保持色相与饱和度。
 * {@code aScale}/{@code bScale} 默认取 1，只由实测拟合给出<b>幅度很小</b>的 {@code aOffset}/{@code bOffset}
 * 来贴合目标色相（例如红杉相对橡木的红通道偏移）。
 * <p>
 * <b>高亮端柔性压缩</b>：{@link #HIGHLIGHT_KNEE} 之上不再线性外推，而是用 {@code tanh} 平滑趋近 1.0，
 * 避免「乘数过大导致高光细节丢失、整片压平」。对木材这类中间调贴图基本不会触发（橡木最亮处 {@code L≈0.7}），
 * 只作为病态输入的保险。
 * <p>
 * 本类为不可变值对象，线程安全；像素处理所需的临时数组通过 {@link ThreadLocal} 复用，避免逐像素分配。
 */
public record OkLabMap(float lScale, float lOffset, float aScale, float aOffset, float bScale, float bOffset) {

    /** 恒等映射（不做任何改动）。 */
    public static final OkLabMap IDENTITY = new OkLabMap(1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);

    /** 高亮端柔性压缩起点；超过该亮度后改用 {@code tanh} 渐近到 1.0。 */
    public static final float HIGHLIGHT_KNEE = 0.92F;

    /** OKLab 三元组与线性 RGB 三元组的线程本地暂存，避免逐像素分配。 */
    private static final ThreadLocal<float[]> LAB_SCRATCH = ThreadLocal.withInitial(() -> new float[3]);
    private static final ThreadLocal<float[]> LINEAR_SCRATCH = ThreadLocal.withInitial(() -> new float[3]);

    /**
     * 构造「把源贴图均值对齐到目标色」的映射——本模组木种重着色的核心入口。
     * <p>
     * 令 {@code L' = k·L + b}，并取 {@code b = targetL - k·meanL}，于是源贴图的加权平均亮度
     * 恰好落到目标色的亮度上；{@code a}/{@code b} 同理。因为这是一个<b>仿射</b>映射，
     * 源贴图内部的明暗对比会被完整保留（只有整体平移与对比度缩放），不会出现「所有像素趋同」的压平。
     * <p>
     * 灰度源贴图（树叶）天然适用：它的 {@code meanA}/{@code meanB} 都是 0，
     * 于是 {@code a' = k·a + targetA} 直接给出目标色相，无需特殊分支。
     *
     * @param targetL         目标色的 OKLab 亮度
     * @param targetA         目标色的 OKLab a
     * @param targetB         目标色的 OKLab b
     * @param sourceMeanL     源贴图的加权平均亮度
     * @param sourceMeanA     源贴图的加权平均 a
     * @param sourceMeanB     源贴图的加权平均 b
     * @param lightnessContrast 明度对比度缩放；1 表示完整保留源贴图明暗层次
     * @param chromaScale     色度缩放；1 表示完整保留源贴图的饱和度起伏
     */
    public static OkLabMap matching(float targetL, float targetA, float targetB,
            float sourceMeanL, float sourceMeanA, float sourceMeanB,
            float lightnessContrast, float chromaScale) {
        return new OkLabMap(
                lightnessContrast, targetL - lightnessContrast * sourceMeanL,
                chromaScale, targetA - chromaScale * sourceMeanA,
                chromaScale, targetB - chromaScale * sourceMeanB);
    }

    public boolean isIdentity() {
        return this.equals(IDENTITY);
    }

    /**
     * 对单个 {@code 0xAARRGGBB} 像素应用映射。
     * <p>
     * alpha 完全为 0 的像素直接原样返回——贴图边缘通常有全透明像素，对它们做颜色变换没有意义，
     * 反而可能污染后续 mipmap 的颜色平均。
     */
    public int applyArgb(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return argb;
        }

        float[] lab = LAB_SCRATCH.get();
        OkLab.fromArgb(argb, lab);

        float lightness = mapLightness(lab[0]);
        float a = this.aScale * lab[1] + this.aOffset;
        float b = this.bScale * lab[2] + this.bOffset;

        float[] linear = LINEAR_SCRATCH.get();
        OkLab.compressToGamut(lightness, a, b, linear);
        return OkLab.pack(alpha, linear[0], linear[1], linear[2]);
    }

    /**
     * 对 {@code 0xAARRGGBB} 像素数组批量应用映射。
     *
     * @param pixels 输入数组，不会被修改
     * @return 新数组；长度与输入一致
     */
    public int[] applyArgb(int[] pixels) {
        int[] out = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            out[i] = applyArgb(pixels[i]);
        }
        return out;
    }

    /** {@code L' = knee(lScale * L + lOffset)}。 */
    private float mapLightness(float lightness) {
        float mapped = this.lScale * lightness + this.lOffset;
        if (mapped <= 0.0F) {
            return 0.0F;
        }
        if (mapped <= HIGHLIGHT_KNEE) {
            return mapped;
        }
        float range = 1.0F - HIGHLIGHT_KNEE;
        float t = (mapped - HIGHLIGHT_KNEE) / range;
        return HIGHLIGHT_KNEE + range * (float) Math.tanh(t);
    }

    /** 便于日志与拟合报告阅读的简短描述。 */
    public String describe() {
        return String.format("L=%.4f*L%+.4f a=%.4f*a%+.4f b=%.4f*b%+.4f",
                this.lScale, this.lOffset, this.aScale, this.aOffset, this.bScale, this.bOffset);
    }
}
