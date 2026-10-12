package team.xcmt.sam.event;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.combat.ElementType;
import team.xcmt.sam.content.ElementEffects;
import team.xcmt.sam.util.CombatDebug;

/**
 * 元素强度的时间衰减与时长归一化。
 * <p>
 * 元素效果的时长恒为 3 秒：到期即意味着「降一级」，因此这里在 {@link MobEffectEvent.Expired}
 * 记录待处理项，并在本 tick 结束时（{@link ServerTickEvent.Post}）以「强度-1、时长 3 秒」重新附着。
 * <p>
 * 之所以延后到 tick 末处理，是因为 {@code Expired} 由 {@code LivingEntity.tickEffects} 在遍历
 * 效果表时触发，就地增删会破坏迭代器。同时该处理器也负责「无视药水效果时长」：任何来源写入的
 * 非 3 秒时长都会被归一化。
 */
@EventBusSubscriber(modid = ScienceAndMagic.MOD_ID)
public final class ElementEffectEvents {

    /** 待处理的强度设置：{@code entity} 最终强度设为 {@code strength}。 */
    private record Pending(LivingEntity entity, ElementType type, int strength) {
    }

    private static final List<Pending> PENDING = new ArrayList<>();

    private ElementEffectEvents() {
    }

    /** 元素强度到期：记录「降一级」。 */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) {
            // 事件允许在实体身上已不存在该效果时触发，此时无需衰减。
            return;
        }
        ElementType type = ElementEffects.typeOf(instance.getEffect());
        if (type != null) {
            // 旧强度 = amplifier + 1，降一级后 = amplifier
            PENDING.add(new Pending(event.getEntity(), type, instance.getAmplifier()));
            CombatDebug.log("元素到期衰减 {} [{}]: 强度 {} → {}（本 tick 末生效）",
                    event.getEntity().getName().getString(), type.id(),
                    instance.getAmplifier() + 1, instance.getAmplifier());
        }
    }

    /** 外部来源写入了不符合约定的时长：归一化为 3 秒（强度不变）。 */
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        ElementType type = ElementEffects.typeOf(instance.getEffect());
        if (type == null || instance.getDuration() == ElementEffects.DURATION_TICKS) {
            return;
        }
        PENDING.add(new Pending(event.getEntity(), type, instance.getAmplifier() + 1));
        CombatDebug.log("元素时长归一化 {} [{}]: duration {} → {}（强度保持 {}）",
                event.getEntity().getName().getString(), type.id(), instance.getDuration(),
                ElementEffects.DURATION_TICKS, instance.getAmplifier() + 1);
    }

    /** tick 末统一落盘，避开效果表的迭代过程。 */
    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) {
            return;
        }
        List<Pending> batch = List.copyOf(PENDING);
        PENDING.clear();
        for (Pending pending : batch) {
            if (pending.entity().isAlive()) {
                ElementEffects.setStrength(pending.entity(), pending.type(), pending.strength());
            }
        }
    }
}
