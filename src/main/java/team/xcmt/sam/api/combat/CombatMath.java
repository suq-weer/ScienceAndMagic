// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.combat;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import team.xcmt.sam.content.CombatAttributes;

/**
 * 战斗数值核心：把玩家属性换算成「最终伤害 / 最终防御」。
 * <p>
 * 全部方法都是纯函数（只读取属性与随机数），便于单元测试与在 Mixin 中调用。
 * <b>取整</b>：最终伤害与最终防御一律 {@link #round(double) 四舍五入}（示例 3.29 → 3）。
 */
public final class CombatMath {

    /** 百分比基准：属性里 100 表示 100%。 */
    public static final double PERCENT = 100.0;

    /** 触发物伤 / 法伤防御率时，多挡住一份 bDEF。 */
    private static final double EXTRA_DEFENSE_MULTIPLIER = 2.0;

    private CombatMath() {
    }

    /** 读取属性值。 */
    public static double attribute(LivingEntity entity, Holder<Attribute> attribute) {
        return entity.getAttributeValue(attribute);
    }

    /**
     * 加权系数：物理用 PE（体魄），法术 / 科技用 MC（意志力），100% 即 ×1。
     */
    public static double weight(LivingEntity entity, DamageCategory category) {
        Holder<Attribute> attribute = category == DamageCategory.PHYSICAL
                ? CombatAttributes.PLAYER_ENERGY
                : CombatAttributes.MAGIC_COUNT;
        return attribute(entity, attribute) / PERCENT;
    }

    /**
     * 攻击加成（百分比数值）：物理 pATK、法术 mATK、科技 sATK。
     */
    public static double attackBonusPercent(LivingEntity entity, DamageCategory category) {
        Holder<Attribute> attribute = switch (category) {
            case PHYSICAL -> CombatAttributes.PHYSIC_ATTACK;
            case MAGIC -> CombatAttributes.MAGIC_ATTACK;
            case TECH -> CombatAttributes.SCIENCE_ATTACK;
        };
        return attribute(entity, attribute);
    }

    /**
     * 伤害第 1 步：由玩家属性算出的最终伤害（含致命加成）。
     * <p>
     * {@code (bATK × (1 + 攻击加成%) + 致命加成) × 加权%}；不含武器 / 魂骨 / 装备 / 元素附加。
     * 与示例 {@code [1×(1+29%)＋(2)]×1 = 3.29} 一致。
     *
     * @param criticalBonus 致命判定得到的加伤（未触发传 0）
     */
    public static double playerAttributeDamage(LivingEntity attacker, DamageCategory category,
            double criticalBonus) {
        double base = attribute(attacker, CombatAttributes.BASE_ATTACK);
        double bonus = attackBonusPercent(attacker, category) / PERCENT;
        return (base * (1.0 + bonus) + criticalBonus) * weight(attacker, category);
    }

    /** 伤害第 1 步（不含致命加成）。 */
    public static double playerAttributeDamage(LivingEntity attacker, DamageCategory category) {
        return playerAttributeDamage(attacker, category, 0.0);
    }

    /**
     * 致命（BLD）判定：触发时返回 {@code [0, BLDk]} 区间内的均匀随机实数，未触发返回 0。
     */
    public static double rollCriticalBonus(LivingEntity attacker, RandomSource random) {
        double rate = attribute(attacker, CombatAttributes.BLOOD_COUNTER);
        double amount = attribute(attacker, CombatAttributes.BLOOD_COUNT);
        if (rate <= 0.0 || amount <= 0.0) {
            return 0.0;
        }
        if (random.nextDouble() * PERCENT < rate) {
            return random.nextDouble() * amount;
        }
        return 0.0;
    }

    /**
     * 防御第 2 步：由玩家防御属性算出的最终防御（减法式）。
     * <p>
     * {@code bDEF ×（触发防御率时 ×2）× 加权%}；不含武器 / 装备 / 魂骨 / 元素附加。
     */
    public static double playerDefense(LivingEntity victim, DamageCategory category,
            RandomSource random) {
        Holder<Attribute> rateAttribute = category == DamageCategory.PHYSICAL
                ? CombatAttributes.PHYSIC_DEFEND
                : CombatAttributes.MAGIC_DEFEND;
        double rate = attribute(victim, rateAttribute);
        boolean extraBlocked = rate > 0.0 && random.nextDouble() * PERCENT < rate;
        double base = attribute(victim, CombatAttributes.BASE_DEFEND);
        return base * (extraBlocked ? EXTRA_DEFENSE_MULTIPLIER : 1.0) * weight(victim, category);
    }

    /**
     * 防御第 8 步：减法式最终受到伤害，四舍五入且不小于 0。
     *
     * @param incoming      发出时的伤害（已四舍五入）
     * @param finalDefense  四舍五入后的最终防御
     */
    public static int finalIncomingDamage(double incoming, double finalDefense) {
        return Math.max(round(incoming) - round(finalDefense), 0);
    }

    /** 元素关系倍率（攻方元素 → 守方元素）。 */
    public static double elementMultiplier(ElementType attacker, ElementType defender) {
        return ElementRelation.multiplier(attacker, defender);
    }

    /** 四舍五入取整。 */
    public static int round(double value) {
        return (int) Math.round(value);
    }
}
