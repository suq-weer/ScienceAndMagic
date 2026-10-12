// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import org.jspecify.annotations.NonNull;

/**
 * 伤害数字粒子：一个只画文字、不带贴图的粒子。
 * <p>
 * <b>为什么重写 {@link #tick()}</b>：原版 {@link Particle#tick()} 会走重力 / 摩擦 / 碰撞，
 * 数字会往下掉、还会被方块挡住。这里改成「匀速上浮 + 轻微水平漂移」，且 {@code hasPhysics = false}
 * 让它完全不受方块影响。
 * <p>
 * 位置 / 缩放 / 颜色都按「存活进度」计算（见 {@link #renderScale(float)} / {@link #renderColor(float)}），
 * 用 {@code partialTick} 插值，因此动画与帧率无关。
 */
public class DamageNumberParticle extends Particle {

    /** 一个「像素」对应的世界坐标长度（文字整体大小的基准）。 */
    static final float TEXT_SCALE = 0.025F;

    private static final int LIFETIME_TICKS = 22;
    private static final float RISE_HEIGHT = 0.85F;
    /** 弹出动画持续的 tick 数。 */
    private static final float POP_TICKS = 3.0F;
    private static final float POP_START_SCALE = 0.6F;
    /** 从存活进度的哪个比例开始淡出。 */
    private static final float FADE_START_PROGRESS = 0.6F;
    private static final float DRIFT_RANGE = 0.12F;
    /** 全亮光照：伤害数字无论白天黑夜、洞里洞外都要看得清。 */
    private static final int FULL_BRIGHT = 0x00F000F0;

    private final FormattedCharSequence text;
    private final int rgb;
    private final float baseScale;
    private final double driftX;
    private final double driftZ;

    DamageNumberParticle(ClientLevel level, double x, double y, double z, float damage, int rgb, float scale,
            RandomSource random) {
        super(level, x, y, z);
        this.hasPhysics = false;
        this.setLifetime(LIFETIME_TICKS);
        // 取整显示：本模组的「最终伤害」本身就是四舍五入的整数，防御结算后的实际扣血也按整数显示更好读。
        this.text = Component.literal(Integer.toString(Math.round(damage))).getVisualOrderText();
        this.rgb = rgb;
        this.baseScale = scale;
        this.driftX = (random.nextFloat() - 0.5F) * DRIFT_RANGE;
        this.driftZ = (random.nextFloat() - 0.5F) * DRIFT_RANGE;
    }

    @Override
    public @NonNull ParticleRenderType getGroup() {
        return DamageNumberParticles.RENDER_TYPE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.age++;
        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }

        // 上浮速度随进度线性衰减（先快后慢），总位移约等于 RISE_HEIGHT。
        float progress = (float) this.age / (float) this.lifetime;
        double riseSpeed = RISE_HEIGHT / (float) this.lifetime * 2.0 * (1.0 - progress);
        this.x += this.driftX;
        this.y += riseSpeed;
        this.z += this.driftZ;
    }

    FormattedCharSequence text() {
        return this.text;
    }

    int light() {
        return FULL_BRIGHT;
    }

    double renderX(float partialTick) {
        return Mth.lerp(partialTick, this.xo, this.x);
    }

    double renderY(float partialTick) {
        return Mth.lerp(partialTick, this.yo, this.y);
    }

    double renderZ(float partialTick) {
        return Mth.lerp(partialTick, this.zo, this.z);
    }

    /** 出现时先小后大地弹一下，之后保持固定大小。 */
    float renderScale(float partialTick) {
        float pop = Mth.clamp((this.age + partialTick) / POP_TICKS, 0.0F, 1.0F);
        return this.baseScale * TEXT_SCALE * Mth.lerp(pop, POP_START_SCALE, 1.0F);
    }

    /** 用带透明度的 ARGB 颜色实现淡出。 */
    int renderColor(float partialTick) {
        float progress = Mth.clamp((this.age + partialTick) / (float) this.lifetime, 0.0F, 1.0F);
        float alpha = progress <= FADE_START_PROGRESS
                ? 1.0F
                : Mth.clamp((1.0F - progress) / (1.0F - FADE_START_PROGRESS), 0.0F, 1.0F);
        return ((int) (alpha * 255.0F) << 24) | this.rgb;
    }
}
