package team.xcmt.sam.content;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.combat.ElementRelation;
import team.xcmt.sam.api.combat.ElementType;
import team.xcmt.sam.util.CombatDebug;

/**
 * 6 种元素附着 MobEffect，以及「多级强度」的读写。
 * <p>
 * <b>元素附着对结算的唯一影响是「克制判定倍率」</b>（固定 1.5 / 0.5 / 1.0，见
 * {@link ElementRelation}）。不存在"每级元素增伤 / 减伤"这类平值——强度只用来：
 * <ul>
 *     <li>决定该实体当前的「生效元素」（取强度最高者）；</li>
 *     <li>作为消耗资源：克制结算后涉事元素各降一级；</li>
 *     <li>随时间自然衰减（每 3 秒降一级）。</li>
 * </ul>
 * <p>
 * <b>强度</b>存在 {@link MobEffectInstance#getAmplifier() amplifier} 中（显示强度 = amplifier + 1）。
 * 附着同元素则强度相加；时长恒为 {@link #DURATION_TICKS}（3 秒），到期即降一级；一切附着 / 叠加 /
 * 降级都强制写成 3 秒，忽略外部传入的时长。所有写操作只在服务端执行。
 * <p>
 * 图标由注册名自动解析：{@code assets/science_and_magic/textures/mob_effect/element_<id>.png}
 * 会被 {@code atlases/gui.json} 的 {@code mob_effect} 目录源收录，无需额外注册。
 */
public final class ElementEffects {

    /** 元素效果注册表。 */
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, ScienceAndMagic.MOD_ID);

    /** 每级强度的持续时间：3 秒。 */
    public static final int DURATION_TICKS = 60;

    /** 强度上限（受原版 amplifier ≤ 255 限制）。 */
    public static final int MAX_STRENGTH = MobEffectInstance.MAX_AMPLIFIER + 1;

    private static final Map<ElementType, DeferredHolder<MobEffect, MobEffect>> EFFECTS =
            new EnumMap<>(ElementType.class);

    static {
        register(ElementType.FIRE, 0xFFE25822);
        register(ElementType.ICE, 0xFF7FD8F7);
        register(ElementType.LIGHTING, 0xFFFFE066);
        register(ElementType.STONE, 0xFF9E8A6E);
        register(ElementType.WATER, 0xFF3A7BD5);
        register(ElementType.WOOD, 0xFF4CAF50);
    }

    private ElementEffects() {
    }

    private static void register(ElementType type, int color) {
        // MobEffect 的构造器是 protected，用匿名子类实例化（与 vanilla 各效果类的做法一致）
        EFFECTS.put(type, MOB_EFFECTS.register(type.effectPath(),
                () -> new MobEffect(MobEffectCategory.BENEFICIAL, color) {
                }));
    }

    /** 元素对应的效果持有者。 */
    public static Holder<MobEffect> effect(ElementType type) {
        return EFFECTS.get(type);
    }

    /** 由效果反查元素，非元素效果返回 {@code null}。 */
    @Nullable
    public static ElementType typeOf(Holder<MobEffect> effect) {
        for (Map.Entry<ElementType, DeferredHolder<MobEffect, MobEffect>> entry : EFFECTS.entrySet()) {
            if (entry.getValue().value() == effect.value()) {
                return entry.getKey();
            }
        }
        return null;
    }

    // ---- 强度读写 ----

    /** 当前强度（0 表示未附着）。 */
    public static int strength(LivingEntity entity, ElementType type) {
        MobEffectInstance instance = entity.getEffect(effect(type));
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    /**
     * 附着元素：若已有同元素则强度相加，并把时长重置为 3 秒。
     *
     * @param levels 本次附着的层数（至少 1）
     */
    public static void attach(LivingEntity entity, ElementType type, int levels) {
        if (levels <= 0) {
            return;
        }
        setStrength(entity, type, strength(entity, type) + levels);
    }

    /** 强度降一级（克制结算与每 3 秒衰减共用）。 */
    public static void decay(LivingEntity entity, ElementType type) {
        setStrength(entity, type, strength(entity, type) - 1);
    }

    /**
     * 直接设置强度：0 表示移除，否则以 amplifier = 强度-1、时长 3 秒重新附着。
     */
    public static void setStrength(LivingEntity entity, ElementType type, int strength) {
        Holder<MobEffect> effect = effect(type);
        MobEffectInstance before = entity.getEffect(effect);
        int beforeStrength = before == null ? 0 : before.getAmplifier() + 1;
        if (beforeStrength == 0 && strength <= 0) {
            return;
        }
        entity.removeEffect(effect);
        if (strength <= 0) {
            CombatDebug.log("元素强度 {} [{}]: {} → 0（移除）",
                    entity.getName().getString(), type.id(), beforeStrength);
            return;
        }
        int amplifier = Mth.clamp(strength - 1, MobEffectInstance.MIN_AMPLIFIER,
                MobEffectInstance.MAX_AMPLIFIER);
        entity.addEffect(new MobEffectInstance(effect, DURATION_TICKS, amplifier, false, true, true));
        CombatDebug.log("元素强度 {} [{}]: {} → {}（amplifier={}, 时长 {} ticks={}s）",
                entity.getName().getString(), type.id(), beforeStrength, strength,
                amplifier, DURATION_TICKS, DURATION_TICKS / 20.0);
    }

    /** 取身上强度最高的元素（即该实体的「生效元素」），无附着返回 {@code null}。 */
    @Nullable
    public static ElementType highest(LivingEntity entity) {
        ElementType best = null;
        int bestAmplifier = -1;
        for (ElementType type : ElementType.values()) {
            MobEffectInstance instance = entity.getEffect(effect(type));
            if (instance != null && instance.getAmplifier() > bestAmplifier) {
                bestAmplifier = instance.getAmplifier();
                best = type;
            }
        }
        return best;
    }

    // ---- 克制关系结算 ----

    /** 攻击方生效元素：优先「伤害携带元素」（武器附着），回退自身最高强度附着。 */
    @Nullable
    public static ElementType attackElement(@Nullable LivingEntity attacker,
            @Nullable ElementAttachmentSpec damageAttachment) {
        if (damageAttachment != null) {
            return damageAttachment.element();
        }
        return attacker == null ? null : highest(attacker);
    }

    /** 只读地计算克制倍率，不改变强度。 */
    public static double relationMultiplier(@Nullable LivingEntity attacker,
            @Nullable LivingEntity victim, @Nullable ElementAttachmentSpec damageAttachment) {
        return ElementRelation.multiplier(attackElement(attacker, damageAttachment),
                victim == null ? null : highest(victim));
    }

    /** 只读地计算克制倍率（无伤害携带元素）。 */
    public static double relationMultiplier(@Nullable LivingEntity attacker,
            @Nullable LivingEntity victim) {
        return relationMultiplier(attacker, victim, null);
    }

    /**
     * 结算一次攻击的元素关系（伤害侧与防守侧共用）。
     * <ol>
     *     <li>取攻方元素（伤害携带优先，否则自身附着）与守方元素（自身最高强度附着）算克制倍率；</li>
     *     <li>产生实际关系时，守方涉事元素降一级；</li>
     *     <li>攻方：<b>伤害携带元素在产生克制关系时按「判定」扣 1 级</b>（附着武器统一二级 → 剩一级）；
     *         <b>不产生关系时不消耗</b>；没有携带元素且产生实际关系时，才消耗自身附着一级；</li>
     *     <li><b>传染</b>：把该元素按「判定后强度」附着到被攻击实体——
     *         产生关系传剩余强度（二级武器 → 1 级），不产生关系传完整强度（二级武器 → 2 级）。</li>
     * </ol>
     * 必须在服务端调用。
     */
    public static double resolveAttackRelation(@Nullable LivingEntity attacker,
            @Nullable LivingEntity victim, @Nullable ElementAttachmentSpec damageAttachment) {
        ElementType attackerElement = attackElement(attacker, damageAttachment);
        ElementType victimElement = victim == null ? null : highest(victim);
        double multiplier = ElementRelation.multiplier(attackerElement, victimElement);

        // 守方：产生实际关系时自身元素降一级
        if (multiplier != 1.0 && victim != null && victimElement != null) {
            decay(victim, victimElement);
        }

        // 攻方：产生克制关系时伤害携带元素按「判定」扣 1 级；不产生关系则不消耗
        int transferStrength = 0;
        if (damageAttachment != null) {
            transferStrength = multiplier != 1.0
                    ? damageAttachment.strength() - 1
                    : damageAttachment.strength();
        } else if (multiplier != 1.0 && attacker != null && attackerElement != null) {
            decay(attacker, attackerElement);
        }

        // 传染：判定后强度不为 0 → 按该强度附着到被攻击实体
        if (damageAttachment != null && victim != null && transferStrength > 0) {
            attach(victim, damageAttachment.element(), transferStrength);
        }

        if (CombatDebug.isEnabled()) {
            String carried;
            if (damageAttachment == null) {
                carried = "无伤害附带元素";
            } else {
                carried = "伤害附带 " + damageAttachment.element().id() + " "
                        + damageAttachment.strength() + " 级 → "
                        + (multiplier != 1.0 ? "判定消耗 1 级，传染 " : "无关系未消耗，传染 ")
                        + Math.max(transferStrength, 0) + " 级"
                        + (transferStrength > 0 ? "给受击者" : "（无剩余，不传染）");
            }
            CombatDebug.log("元素克制结算 {} [{}] → {} [{}]: 倍率={} （{}）",
                    attacker == null ? "无攻方" : attacker.getName().getString(), attackerElement,
                    victim == null ? "无守方" : victim.getName().getString(), victimElement,
                    multiplier, carried);
        }
        return multiplier;
    }

    /** 结算克制关系（无伤害携带元素）。 */
    public static double resolveRelation(@Nullable LivingEntity attacker,
            @Nullable LivingEntity victim) {
        return resolveAttackRelation(attacker, victim, null);
    }

    /** 全部元素（用于遍历）。 */
    public static List<ElementType> all() {
        return List.of(ElementType.values());
    }
}
