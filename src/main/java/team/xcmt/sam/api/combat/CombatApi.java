// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.combat;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import team.xcmt.sam.api.combat.event.SamAttackEvent;
import team.xcmt.sam.api.combat.event.SamDefenseEvent;
import team.xcmt.sam.content.CombatAttributes;
import team.xcmt.sam.content.ElementEffects;

/**
 * 兼容适配层对外门面：其他模组通过它接入本模组的战斗机制。
 * <p>
 * 由于本模组<b>删除了原版玩家攻击伤害与跳劈暴击</b>，第三方若只依赖原版行为会失效。为此提供：
 * <ul>
 *     <li>{@link #registerContributor}：注册附加伤害 / 附加防御（对应结算的各阶段）；</li>
 *     <li>{@link #registerTechDamageType}：把某个伤害类型标记为科技类（sATK）；</li>
 *     <li>{@link #attachElement}/{@link #elementStrength}/{@link #highestElement}：读写元素附着强度；</li>
 *     <li>事件 {@link SamAttackEvent} / {@link SamDefenseEvent}（{@code NeoForge.EVENT_BUS}）；</li>
 *     <li>原版 {@code CriticalHitEvent} 仍在每次玩家攻击时触发，本模组把自身致命判定作为
 *         「原版暴击」传入，第三方的暴击修改会被采纳。</li>
 * </ul>
 * 属性本身通过 {@link CombatAttributes} 的 {@code DeferredHolder} 暴露，可直接用于
 * {@code AttributeModifier}。
 */
public final class CombatApi {

    private CombatApi() {
    }

    /** 注册一个附加伤害 / 附加防御的贡献者。 */
    public static void registerContributor(CombatContributor contributor) {
        CombatContributors.register(contributor);
    }

    /** 把某个伤害类型登记为科技类（仅归类，sATK 结算为后续内容）。 */
    public static void registerTechDamageType(ResourceKey<DamageType> damageType) {
        DamageClassifier.registerTech(damageType);
    }

    /** 归类一次伤害。 */
    public static DamageCategory damageCategory(DamageSource source) {
        return DamageClassifier.classify(source);
    }

    /** 读取某个元素的附着强度（0 表示未附着）。 */
    public static int elementStrength(LivingEntity entity, ElementType element) {
        return ElementEffects.strength(entity, element);
    }

    /** 取身上强度最高的元素，无附着返回 {@code null}。 */
    @Nullable
    public static ElementType highestElement(LivingEntity entity) {
        return ElementEffects.highest(entity);
    }

    /**
     * 附着元素：已有同元素则强度相加，时长重置为 3 秒。
     * <p>
     * 只在服务端调用有效。
     */
    public static void attachElement(LivingEntity entity, ElementType element, int levels) {
        ElementEffects.attach(entity, element, levels);
    }

    /** 元素强度降一级（耗时 / 克制消耗）。 */
    public static void decayElement(LivingEntity entity, ElementType element) {
        ElementEffects.decay(entity, element);
    }

    /** 读取玩家某项战斗属性（便于第三方做数值展示 / 判断）。 */
    public static double attribute(LivingEntity entity, Holder<Attribute> attribute) {
        return entity.getAttributeValue(attribute);
    }

    /** 便于第三方判断「这次攻击是不是玩家发起的」。 */
    public static boolean isPlayerAttack(DamageSource source) {
        return source.getEntity() instanceof Player;
    }
}
