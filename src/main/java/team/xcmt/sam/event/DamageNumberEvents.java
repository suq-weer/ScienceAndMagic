// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.event;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.combat.event.SamAttackEvent;
import team.xcmt.sam.content.particle.DamageNumberParticleOptions;
import team.xcmt.sam.util.CombatDebug;

/**
 * 在生物受伤时弹出伤害数字。
 * <p>
 * <b>数值口径</b>：取 {@link LivingDamageEvent.Post#getHealthDamage()}，即「最终真正掉的血」
 * （已经过防御、抗性、吸收等全部结算）。若这次伤害一点血都没掉（例如全被伤害吸收吃掉），
 * 退而使用 {@link LivingDamageEvent.Post#getInflictedDamage()}，避免玩家看到「打了却没数字」。
 * <p>
 * <b>致命着色</b>：{@link SamAttackEvent} 在「发出伤害」之前触发且带着本模组的致命判定结果，
 * 但 {@link LivingDamageEvent.Post} 里拿不到它，所以这里用一张「即将结算的致命标记表」把状态
 * 带过去（同一 tick、同一线程；{@link ServerTickEvent.Post} 负责清理没被消费的过期标记）。
 * <p>
 * 数字广播给附近所有玩家（{@code overrideLimiter = true}），因此远处的观察者也能看到。
 */
@EventBusSubscriber(modid = ScienceAndMagic.MOD_ID)
public final class DamageNumberEvents {

    /** 普通伤害：白色。 */
    private static final int COLOR_NORMAL = 0xFFFFFF;
    /** 致命伤害：金色。 */
    private static final int COLOR_CRITICAL = 0xFFC93C;
    private static final float SCALE_NORMAL = 1.0F;
    private static final float SCALE_CRITICAL = 1.35F;
    /** 数字从身体的这个高度比例处弹出（0 = 脚底，1 = 头顶）。 */
    private static final float SPAWN_HEIGHT_RATIO = 0.6F;
    /** 致命标记的有效 tick 数；超过即视为过期，防止攻击被取消时残留。 */
    private static final long CRITICAL_MARK_TICKS = 2L;

    /** 目标 → 标记时间（游戏刻）。仅在服务端线程访问。 */
    private static final Map<LivingEntity, Long> CRITICAL_MARKS = new IdentityHashMap<>();

    private DamageNumberEvents() {
    }

    /** 记录「下一次结算如果致命」——由本模组攻击管线在发出伤害前触发。 */
    @SubscribeEvent
    public static void onSamAttack(SamAttackEvent event) {
        if (!event.isCritical() || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        if (target.level().isClientSide()) {
            return;
        }
        CRITICAL_MARKS.put(target, target.level().getGameTime());
    }

    /** 伤害落地后弹数字。 */
    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        float damage = event.getHealthDamage();
        if (damage <= 0.0F) {
            damage = event.getInflictedDamage();
        }
        if (damage <= 0.0F) {
            return;
        }

        boolean critical = consumeCritical(entity, level.getGameTime());
        level.sendParticles(
                new DamageNumberParticleOptions(damage, critical ? COLOR_CRITICAL : COLOR_NORMAL,
                        critical ? SCALE_CRITICAL : SCALE_NORMAL),
                true, true,
                entity.getX(), entity.getY() + entity.getBbHeight() * SPAWN_HEIGHT_RATIO, entity.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);

        CombatDebug.log("伤害数字: 实体={} 扣血={} 致命={}", entity.getName().getString(),
                String.format("%.2f", damage), critical);
    }

    /**
     * 清理攻击了却始终没落地的致命标记（例如攻击被取消 / 伤害为 0）。
     * <p>
     * 取主世界的 {@code gameTime} 当单调时钟：各维度的 gameTime 同步推进，且主世界必然存在。
     * 该 {@code ServerLevel} 归服务端持有并在关服时统一回收，这里只读，<b>不得</b>关闭它。
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (CRITICAL_MARKS.isEmpty()) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        CRITICAL_MARKS.entrySet().removeIf(entry -> now - entry.getValue() > CRITICAL_MARK_TICKS);
    }

    private static boolean consumeCritical(LivingEntity entity, long gameTime) {
        Long marked = CRITICAL_MARKS.remove(entity);
        return marked != null && gameTime - marked <= CRITICAL_MARK_TICKS;
    }
}
