package team.xcmt.sam.util;

/**
 * OKLab 色彩空间工具。
 * <p>
 * OKLab（Björn Ottosson）是感知均匀的色彩空间，{@code L} 表示感知亮度、{@code a}/{@code b} 表示色度方向。
 * 在它上面只改 {@code L} 就能得到「同色相、不同明度」的重着色效果，比在 gamma 编码的 sRGB 上直接做乘法
 * 更符合人眼感知，也不会把暗部压死。
 * <p>
 * <b>关键约定</b>：OKLab 的定义基于<b>线性</b> sRGB，因此进出本类时必须先做
 * {@link #srgbToLinear(float)} / {@link #linearToSrgb(float)}。直接对 gamma 编码值套矩阵会产生严重失真。
 * <p>
 * <b>色域压缩</b>：重映射后若结果落在 sRGB 色域之外，本类不做逐通道裁剪（clipping）——裁剪会在高亮区
 * 严重扭曲颜色。取而代之的是 {@link #compressToGamut(float, float, float, float[])}：保持 {@code L} 与色相不变，
 * 等比缩小 {@code (a, b)} 直到落入色域，把「过饱和」转化为「降 chroma」，感知代价最小。
 * <p>
 * 本类不依赖任何客户端类，运行时与数据生成阶段共用同一份实现，保证两处颜色不漂移。
 */
public final class OkLab {

    /** 判断是否落在色域内时允许的浮点误差。 */
    private static final float GAMUT_EPSILON = 1.0e-5F;

    /** chroma 压缩的二分迭代次数；16 次已能把缩放系数的误差压到 1e-5 量级。 */
    private static final int CHROMA_COMPRESSION_STEPS = 16;

    // ---- 线性 sRGB -> OKLab ----
    private static final float L_R = 0.4122214708F;
    private static final float L_G = 0.5363325363F;
    private static final float L_B = 0.0514459929F;
    private static final float M_R = 0.2119034982F;
    private static final float M_G = 0.6806995451F;
    private static final float M_B = 0.1073969566F;
    private static final float S_R = 0.0883024619F;
    private static final float S_G = 0.2817188376F;
    private static final float S_B = 0.6299787005F;

    private static final float OK_L_L = 0.2104542553F;
    private static final float OK_L_M = 0.7936177850F;
    private static final float OK_L_S = -0.0040720468F;
    private static final float OK_A_L = 1.9779984951F;
    private static final float OK_A_M = -2.4285922050F;
    private static final float OK_A_S = 0.4505937099F;
    private static final float OK_B_L = 0.0259040371F;
    private static final float OK_B_M = 0.7827717662F;
    private static final float OK_B_S = -0.8086757660F;

    // ---- OKLab -> 线性 sRGB ----
    private static final float UN_L_A = 0.3963377774F;
    private static final float UN_L_B = 0.2158037573F;
    private static final float UN_M_A = -0.1055613458F;
    private static final float UN_M_B = -0.0638541728F;
    private static final float UN_S_A = -0.0894841775F;
    private static final float UN_S_B = -1.2914855480F;

    private static final float R_L = 4.0767416621F;
    private static final float R_M = -3.3077115913F;
    private static final float R_S = 0.2309699292F;
    private static final float G_L = -1.2684380046F;
    private static final float G_M = 2.6097574011F;
    private static final float G_S = -0.3413193965F;
    private static final float B_L = -0.0041960863F;
    private static final float B_M = -0.7034186147F;
    private static final float B_S = 1.7076147010F;

    private OkLab() {
    }

    // ------------------------------------------------------------------
    // sRGB <-> 线性 sRGB
    // ------------------------------------------------------------------

    /** gamma 编码的 sRGB 通道（0..1）转线性（0..1）。 */
    public static float srgbToLinear(float channel) {
        if (channel <= 0.04045F) {
            return channel / 12.92F;
        }
        return (float) Math.pow((channel + 0.055F) / 1.055F, 2.4D);
    }

    /** 线性通道（0..1）转 gamma 编码的 sRGB（0..1）。 */
    public static float linearToSrgb(float channel) {
        if (channel <= 0.0031308F) {
            return channel * 12.92F;
        }
        return 1.055F * (float) Math.pow(channel, 1.0D / 2.4D) - 0.055F;
    }

    // ------------------------------------------------------------------
    // 线性 sRGB <-> OKLab
    // ------------------------------------------------------------------

    /**
     * 线性 sRGB 转 OKLab。
     *
     * @param out 结果数组，写入 {@code [L, a, b]}；长度至少 3
     */
    public static void fromLinearSrgb(float r, float g, float b, float[] out) {
        float l = L_R * r + L_G * g + L_B * b;
        float m = M_R * r + M_G * g + M_B * b;
        float s = S_R * r + S_G * g + S_B * b;

        float lc = (float) Math.cbrt(l);
        float mc = (float) Math.cbrt(m);
        float sc = (float) Math.cbrt(s);

        out[0] = OK_L_L * lc + OK_L_M * mc + OK_L_S * sc;
        out[1] = OK_A_L * lc + OK_A_M * mc + OK_A_S * sc;
        out[2] = OK_B_L * lc + OK_B_M * mc + OK_B_S * sc;
    }

    /**
     * OKLab 转线性 sRGB。结果可能超出 {@code [0, 1]}（表示落在 sRGB 色域之外），
     * 调用方应交给 {@link #compressToGamut(float, float, float, float[])} 或自行裁剪。
     *
     * @param out 结果数组，写入 {@code [r, g, b]}；长度至少 3
     */
    public static void toLinearSrgb(float lightness, float a, float b, float[] out) {
        float lc = lightness + UN_L_A * a + UN_L_B * b;
        float mc = lightness + UN_M_A * a + UN_M_B * b;
        float sc = lightness + UN_S_A * a + UN_S_B * b;

        float l = lc * lc * lc;
        float m = mc * mc * mc;
        float s = sc * sc * sc;

        out[0] = R_L * l + R_M * m + R_S * s;
        out[1] = G_L * l + G_M * m + G_S * s;
        out[2] = B_L * l + B_M * m + B_S * s;
    }

    // ------------------------------------------------------------------
    // ARGB 便捷入口
    // ------------------------------------------------------------------

    /**
     * {@code 0xAARRGGBB}（sRGB）转 OKLab，忽略 alpha。
     *
     * @param out 结果数组，写入 {@code [L, a, b]}；长度至少 3
     */
    public static void fromArgb(int argb, float[] out) {
        float r = srgbToLinear(((argb >> 16) & 0xFF) / 255.0F);
        float g = srgbToLinear(((argb >> 8) & 0xFF) / 255.0F);
        float b = srgbToLinear((argb & 0xFF) / 255.0F);
        fromLinearSrgb(r, g, b, out);
    }

    /**
     * OKLab 转 {@code 0xAARRGGBB}（sRGB），过程中做色域压缩，不会裁剪。
     *
     * @param alpha 0..255
     */
    public static int toArgb(float lightness, float a, float b, int alpha) {
        float[] linear = new float[3];
        compressToGamut(lightness, a, b, linear);
        return pack(alpha, linear[0], linear[1], linear[2]);
    }

    /** 把线性 RGB 通道夹到 {@code [0, 1]}、转 sRGB 并打包为 ARGB。 */
    public static int pack(int alpha, float linearR, float linearG, float linearB) {
        int r = toChannel(linearR);
        int g = toChannel(linearG);
        int b = toChannel(linearB);
        return ((alpha & 0xFF) << 24) | (r << 16) | (g << 8) | b;
    }

    private static int toChannel(float linear) {
        float value = linearToSrgb(clamp01(linear));
        return Math.round(value * 255.0F);
    }

    // ------------------------------------------------------------------
    // 色域压缩
    // ------------------------------------------------------------------

    /** 线性 RGB 是否落在 sRGB 色域内。 */
    public static boolean isInGamut(float r, float g, float b) {
        return isChannelInGamut(r) && isChannelInGamut(g) && isChannelInGamut(b);
    }

    private static boolean isChannelInGamut(float channel) {
        return channel >= -GAMUT_EPSILON && channel <= 1.0F + GAMUT_EPSILON;
    }

    /**
     * 把 {@code (L, a, b)} 映射进 sRGB 色域：保持 {@code L} 与色相，等比缩放 chroma
     * {@code (a, b)} 直到入界；已是合法颜色时不做任何改动。
     *
     * @param outLinear 写入线性 sRGB，长度至少 3
     */
    public static void compressToGamut(float lightness, float a, float b, float[] outLinear) {
        float clampedL = clamp01(lightness);

        toLinearSrgb(clampedL, a, b, outLinear);
        if (isInGamut(outLinear[0], outLinear[1], outLinear[2])) {
            return;
        }

        // 中立灰（chroma = 0）必然是合法的：线性灰 = L^3 ∈ [0, 1]。因此在 [0, 1] 上二分必然收敛。
        float low = 0.0F;
        float high = 1.0F;
        for (int i = 0; i < CHROMA_COMPRESSION_STEPS; i++) {
            float mid = (low + high) * 0.5F;
            toLinearSrgb(clampedL, a * mid, b * mid, outLinear);
            if (isInGamut(outLinear[0], outLinear[1], outLinear[2])) {
                low = mid;
            } else {
                high = mid;
            }
        }
        toLinearSrgb(clampedL, a * low, b * low, outLinear);
    }

    private static float clamp01(float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        return Math.min(value, 1.0F);
    }
}
