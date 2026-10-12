package team.xcmt.sam.content;

import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

import team.xcmt.sam.api.combat.CombatContributors;
import team.xcmt.sam.api.combat.CombatMath;
import team.xcmt.sam.api.combat.CombatStage;
import team.xcmt.sam.api.combat.DamageCategory;
import team.xcmt.sam.api.combat.DamageClassifier;
import team.xcmt.sam.api.combat.ElementType;
import team.xcmt.sam.api.combat.event.SamAttackEvent;
import team.xcmt.sam.api.combat.event.SamDefenseEvent;
import team.xcmt.sam.util.CombatDebug;

/**
 * 把「玩家属性 → 最终伤害 / 最终防御」按机制文档的步骤串起来。
 * <p>
 * 伤害 7 步：玩家属性（含致命）→ 武器 → 魂骨 → 装备 → 元素附着 → 原版 → 发出伤害；<br>
 * 防御 8 步：接收伤害 → 玩家防御属性 → 武器 → 装备 → 魂骨 → 元素附着 → 原版 → 最终受到伤害。
 * <p>
 * <b>兼容适配层</b>：
 * <ul>
 *     <li>原版 {@link CriticalHitEvent} 依然每次攻击触发，本模组把自身的致命判定当作「原版暴击状态」
 *         传入，第三方对暴击状态 / 倍率的修改会被采纳；</li>
 *     <li>新增 {@link SamAttackEvent} / {@link SamDefenseEvent}，供第三方读写新机制的结果；</li>
 *     <li>{@link CombatContributors} 提供附加伤害 / 附加防御的注册点。</li>
 * </ul>
 * <p>
 * 侦测日志统一前缀 {@code [SAM-COMBAT]}，开关见 {@link CombatDebug}。
 */
public final class CombatPipeline {

    /** 一次攻击的结算结果。 */
    public record AttackResult(int damage, boolean critical) {
    }

    /** @param damage 最终受到伤害；{@code null} 表示放弃本系统，回退原版护甲公式 */
    public record DefenseResult(@Nullable Float damage) {
    }

    /** 同一 tick、同一线程内一次攻击的上下文（致命判定结果 + 第三方暴击事件）。 */
    private static final class AttackContext {
        private Player attacker;
        private Entity target;
        private boolean critical;
        private double criticalBonus;
        private CriticalHitEvent criticalEvent;
    }

    /** 攻击上下文在「致命判定」与「发出伤害」两处 Mixin 注入点之间传递。 */
    private static final ThreadLocal<AttackContext> CONTEXT = new ThreadLocal<>();

    private CombatPipeline() {
    }

    /**
     * 致命判定入口，供 Mixin 在 {@code CommonHooks.fireCriticalHit} 处调用。
     *
     * @return 本模组判定的致命状态（作为「原版暴击」传给 {@link CriticalHitEvent}）
     */
    public static boolean beginAttack(Player attacker, Entity target) {
        double rate = CombatMath.attribute(attacker, CombatAttributes.BLOOD_COUNTER);
        double amount = CombatMath.attribute(attacker, CombatAttributes.BLOOD_COUNT);
        double bonus = CombatMath.rollCriticalBonus(attacker, attacker.getRandom());

        AttackContext context = new AttackContext();
        context.attacker = attacker;
        context.target = target;
        context.critical = bonus > 0.0;
        context.criticalBonus = bonus;
        CONTEXT.set(context);

        if (CombatDebug.isEnabled()) {
            CombatDebug.log("致命判定 {} → {}: BLD={}% BLDk={} 掷出加伤={} 致命={}",
                    attacker.getName().getString(), target.getName().getString(),
                    rate, amount, String.format("%.3f", bonus), context.critical);
        }
        return context.critical;
    }

    /** 记录 {@link CriticalHitEvent} 的最终结果，供伤害结算采纳第三方的修改。 */
    public static void applyCriticalEvent(CriticalHitEvent event) {
        AttackContext context = CONTEXT.get();
        if (context != null) {
            context.criticalEvent = event;
        }
        if (CombatDebug.isEnabled()) {
            CombatDebug.log("CriticalHitEvent(兼容层): 玩家={} 致命={} 倍率={} 原版致命={} 原版倍率={}",
                    event.getEntity().getName().getString(), event.isCriticalHit(),
                    event.getDamageMultiplier(), event.isVanillaCritical(),
                    event.getVanillaMultiplier());
        }
    }

    /**
     * 伤害 1~7 步。
     *
     * @return 四舍五入后的最终伤害与是否触发致命
     */
    public static AttackResult computeAttack(Player attacker, Entity target, DamageSource source) {
        AttackContext context = CONTEXT.get();
        CONTEXT.remove();

        double criticalBonus;
        boolean critical;
        float multiplier = 1.0F;
        if (context != null && context.attacker == attacker && context.target == target) {
            critical = context.critical;
            criticalBonus = context.criticalBonus;
            if (context.criticalEvent != null) {
                // 兼容层：采纳第三方对暴击状态与倍率的修改
                critical = context.criticalEvent.isCriticalHit();
                multiplier = context.criticalEvent.getDamageMultiplier();
            }
        } else {
            criticalBonus = CombatMath.rollCriticalBonus(attacker, attacker.getRandom());
            critical = criticalBonus > 0.0;
        }
        if (!critical) {
            criticalBonus = 0.0;
            multiplier = 1.0F;
        }

        DamageCategory category = DamageClassifier.classify(source);
        LivingEntity livingTarget = target instanceof LivingEntity living ? living : null;

        // 第 1 步：玩家属性 + 致命
        double damage = CombatMath.playerAttributeDamage(attacker, category, criticalBonus);
        double step1 = damage;

        // 第 2~4 步：武器 / 魂骨 / 装备附加
        double weapon = CombatContributors.attackBonus(attacker, livingTarget, category,
                CombatStage.ATTACK_WEAPON);
        double soulBone = CombatContributors.attackBonus(attacker, livingTarget, category,
                CombatStage.ATTACK_SOUL_BONE);
        double equipment = CombatContributors.attackBonus(attacker, livingTarget, category,
                CombatStage.ATTACK_EQUIPMENT);
        damage += weapon + soulBone + equipment;

        // 第 5 步：元素附着加成 = 克制判定倍率（固定 1.5 / 0.5 / 1.0）
        damage += CombatContributors.attackBonus(attacker, livingTarget, category,
                CombatStage.ATTACK_ELEMENT);

        // 元素来源：优先伤害携带（武器附着），回退攻击者自身附着
        ElementAttachmentSpec damageAttachment = ElementAttachmentSpec.of(source);
        ElementType attackerElement = ElementEffects.attackElement(attacker, damageAttachment);
        ElementType targetElement = livingTarget == null ? null : ElementEffects.highest(livingTarget);
        double relation;
        if (attacker.level() instanceof ServerLevel) {
            // 服务端：结算克制、消耗攻方附着 1 级，并按剩余强度传染给受击者
            relation = ElementEffects.resolveAttackRelation(attacker, livingTarget, damageAttachment);
        } else {
            // 客户端只做表现预测，不改变任何强度
            relation = ElementEffects.relationMultiplier(attacker, livingTarget, damageAttachment);
        }
        damage *= relation;

        // 兼容层：第三方暴击倍率
        damage *= multiplier;

        // 第 6/7 步：原版效果已由本系统接管，直接发出伤害
        int finalDamage = CombatMath.round(damage);
        SamAttackEvent event = new SamAttackEvent(attacker, target, source, category, finalDamage,
                critical);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            CombatDebug.log("SamAttackEvent: 被第三方取消 → 本次攻击不造成伤害");
            return new AttackResult(0, false);
        }
        int resultDamage = CombatMath.round(event.getDamage());

        if (CombatDebug.isEnabled()) {
            CombatDebug.log("攻击 {} → {} | 来源={} 归类={} 端={}",
                    attacker.getName().getString(), target.getName().getString(),
                    source.getMsgId(), category,
                    attacker.level().isClientSide() ? "client" : "server");
            CombatDebug.log("  属性段 bATK={} 加成属性={}% 加权={}% | 致命={} 加伤={} | 1步={}",
                    CombatMath.attribute(attacker, CombatAttributes.BASE_ATTACK),
                    CombatMath.attackBonusPercent(attacker, category),
                    String.format("%.2f", CombatMath.weight(attacker, category) * 100.0),
                    critical, String.format("%.3f", criticalBonus),
                    String.format("%.3f", step1));
            CombatDebug.log("  附加 武器={} 魂骨={} 装备={} | 元素克制 {} → {} 倍率={} 第三方倍率={}",
                    weapon, soulBone, equipment,
                    attackerElement, targetElement, relation, multiplier);
            CombatDebug.log("  元素来源: {}",
                    damageAttachment == null
                            ? "攻击者自身附着 " + attackerElement
                            : "武器/伤害附着 " + damageAttachment.element().id() + " "
                                    + damageAttachment.strength() + " 级");
            CombatDebug.log("  最终伤害={} (SamAttackEvent 后={}, 致命={})",
                    finalDamage, resultDamage, event.isCritical());
        }
        return new AttackResult(resultDamage, event.isCritical());
    }

    /**
     * 防御 2~8 步。
     * <p>
     * 仅用于玩家受击；非玩家实体仍走原版护甲公式。元素克制倍率只在攻击方不是玩家时结算，
     * 避免与 {@link #computeAttack} 的伤害侧重复扣减强度。
     *
     * @param incoming 接收到的伤害
     * @return 结算结果；{@link DefenseResult#damage()} 为 {@code null} 时表示回退原版护甲公式
     */
    public static DefenseResult computeDefense(Player victim, DamageSource source, float incoming) {
        DamageCategory category = DamageClassifier.classify(source);

        // 第 2 步：玩家防御属性
        double defense = CombatMath.playerDefense(victim, category, victim.getRandom());
        double step2 = defense;

        // 第 3~5 步：武器 / 装备 / 魂骨防御
        double weapon = CombatContributors.defenseBonus(victim, source, category,
                CombatStage.DEFENSE_WEAPON);
        double equipment = CombatContributors.defenseBonus(victim, source, category,
                CombatStage.DEFENSE_EQUIPMENT);
        double soulBone = CombatContributors.defenseBonus(victim, source, category,
                CombatStage.DEFENSE_SOUL_BONE);
        defense += weapon + equipment + soulBone;

        // 第 6 步：元素附着加成 / 减益 = 克制判定倍率（固定 1.5 / 0.5 / 1.0）
        defense += CombatContributors.defenseBonus(victim, source, category,
                CombatStage.DEFENSE_ELEMENT);

        double damage = Math.max(incoming - CombatMath.round(defense), 0.0);

        // 攻击方不是玩家时，元素关系在防守侧结算（玩家攻击已在伤害侧结算过）
        double relation = 1.0;
        ElementType attackerElement = null;
        ElementType victimElement = ElementEffects.highest(victim);
        ElementAttachmentSpec damageAttachment = null;
        if (!(source.getEntity() instanceof Player)) {
            LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
            damageAttachment = ElementAttachmentSpec.of(source);
            attackerElement = ElementEffects.attackElement(attacker, damageAttachment);
            if (victim.level() instanceof ServerLevel) {
                relation = ElementEffects.resolveAttackRelation(attacker, victim, damageAttachment);
            } else {
                relation = ElementEffects.relationMultiplier(attacker, victim, damageAttachment);
            }
            damage *= relation;
        }

        // 第 7 步原版效果由 getDamageAfterMagicAbsorb 继续处理；第 8 步：最终受到伤害
        double rounded = CombatMath.round(damage);
        SamDefenseEvent event = new SamDefenseEvent(victim, source, category, incoming, defense,
                rounded);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            CombatDebug.log("SamDefenseEvent: 被第三方取消 → 回退原版护甲公式");
            return new DefenseResult(null);
        }
        if (event.getDefense() != defense && event.getDamage() == rounded) {
            // 第三方只改了防御：按减法式自动重算最终伤害
            event.setDamage(Math.max(incoming - CombatMath.round(event.getDefense()), 0.0));
        }
        int resultDamage = CombatMath.round(event.getDamage());

        if (CombatDebug.isEnabled()) {
            Entity attackerEntity = source.getEntity();
            CombatDebug.log("受击 {} ← {} | 来源={} 归类={} 端={}",
                    victim.getName().getString(),
                    attackerEntity == null ? "环境" : attackerEntity.getName().getString(),
                    source.getMsgId(), category,
                    victim.level().isClientSide() ? "client" : "server");
            CombatDebug.log("  接收={} | 防御段 bDEF={} 防御率属性={}% 加权={}% | 2步={}",
                    incoming,
                    CombatMath.attribute(victim, CombatAttributes.BASE_DEFEND),
                    CombatMath.attribute(victim, category == DamageCategory.PHYSICAL
                            ? CombatAttributes.PHYSIC_DEFEND : CombatAttributes.MAGIC_DEFEND),
                    String.format("%.2f", CombatMath.weight(victim, category) * 100.0),
                    String.format("%.3f", step2));
            CombatDebug.log("  附加防御 武器={} 装备={} 魂骨={} | 元素克制 {} → {} 倍率={}",
                    weapon, equipment, soulBone,
                    attackerElement, victimElement, relation);
            if (damageAttachment != null) {
                CombatDebug.log("  伤害附带元素: {} {} 级",
                        damageAttachment.element().id(), damageAttachment.strength());
            }
            CombatDebug.log("  最终防御={} (四舍五入 {}) 最终受到伤害={}",
                    String.format("%.3f", event.getDefense()),
                    CombatMath.round(event.getDefense()), resultDamage);
        }
        return new DefenseResult((float) resultDamage);
    }

    /**
     * 致命表现：原版跳劈暴击被移除后，由本系统在触发致命时补上音效与粒子。
     */
    public static void playCriticalEffects(Player attacker, Entity target) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(),
                10, 0.2, 0.2, 0.2, 0.2);
    }
}
