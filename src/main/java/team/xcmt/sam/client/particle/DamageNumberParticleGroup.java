package team.xcmt.sam.client.particle;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.NonNull;

/**
 * 伤害数字的粒子分组：把同组的粒子收集成若干「一行文字」，再交给 26.2 的提交管线画出来。
 * <p>
 * 26.2 世界渲染改成了「先提取、再提交」两段式：{@link Particle} 已经<b>没有</b>
 * {@code render(...)} 方法，画什么由 {@code ParticleGroup#extractRenderState} 决定。
 * 这里照搬原版 {@code ItemPickupParticleGroup} 的结构：
 * <ul>
 *     <li>{@link #extractRenderState} 把粒子换算成「相机相对坐标 + 文本 + 样式」；</li>
 *     <li>{@link State#submit} 里用 {@code SubmitNodeCollector#submitText} 提交文字，
 *         字体即 {@link Minecraft#font}（{@code Minecraft.font}）。</li>
 * </ul>
 * 文字朝向相机的做法与 {@code GizmoFeatureRenderer} 一致：平移到相机相对位置 → 应用相机朝向
 * → 以 {@code scale(s, -s, s)} 缩放（y 取负，文字才不会上下颠倒）。
 */
public class DamageNumberParticleGroup extends ParticleGroup<DamageNumberParticle> {

    public DamageNumberParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public @NonNull ParticleGroupRenderState extractRenderState(@NonNull Frustum frustum, @NonNull Camera camera,
            float partialTickTime) {
        Vec3 cameraPos = camera.position();
        List<Line> lines = new ArrayList<>(this.particles.size());
        for (DamageNumberParticle particle : this.particles) {
            double x = particle.renderX(partialTickTime);
            double y = particle.renderY(partialTickTime);
            double z = particle.renderZ(partialTickTime);
            if (!frustum.pointInFrustum(x, y, z)) {
                continue;
            }
            lines.add(new Line(x - cameraPos.x(), y - cameraPos.y(), z - cameraPos.z(), particle.text(),
                    particle.renderScale(partialTickTime), particle.renderColor(partialTickTime), particle.light()));
        }
        return new State(List.copyOf(lines));
    }

    /** 一个待绘制的数字（已是相机相对坐标）。 */
    private record Line(double x, double y, double z, FormattedCharSequence text, float scale, int color, int light) {
    }

    /** 本帧的绘制数据；{@code submit} 在渲染线程被调用。 */
    private record State(List<Line> lines) implements ParticleGroupRenderState {

        @Override
        public void submit(@NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
            if (this.lines.isEmpty()) {
                return;
            }
            Font font = Minecraft.getInstance().font;
            PoseStack poseStack = new PoseStack();
            for (Line line : this.lines) {
                poseStack.pushPose();
                poseStack.translate((float) line.x(), (float) line.y(), (float) line.z());
                poseStack.mulPose(camera.orientation);
                poseStack.scale(line.scale(), -line.scale(), line.scale());
                submitNodeCollector.submitText(poseStack, -font.width(line.text()) / 2.0F, 0.0F, line.text(),
                        true, Font.DisplayMode.SEE_THROUGH, line.light(), line.color(), 0, 0);
                poseStack.popPose();
            }
        }
    }
}
