// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 战斗附加量扩展点。
 * <p>
 * 武器 / 魂骨 / 装备 / 元素附着的附加伤害与附加防御都通过该接口插入，
 * 默认返回 0，因此未注册任何实现时结算结果只由玩家属性决定。
 */
public interface CombatContributor {

    /**
     * 攻击阶段的附加伤害。
     *
     * @param attacker 攻击者
     * @param target   被攻击者
     * @param category 伤害类别
     * @param stage    当前阶段（{@link CombatStage#ATTACK_WEAPON} 等）
     * @return 附加到伤害上的平值
     */
    @SuppressWarnings("SameReturnValue") // 约定：不覆写即视为「无附加量」
    default double attackBonus(Player attacker, LivingEntity target, DamageCategory category,
            CombatStage stage) {
        return 0.0;
    }

    /**
     * 防御阶段的附加防御。
     *
     * @param victim   被攻击者
     * @param source   伤害来源
     * @param category 伤害类别
     * @param stage    当前阶段（{@link CombatStage#DEFENSE_EQUIPMENT} 等）
     * @return 追加到最终防御上的平值
     */
    @SuppressWarnings("SameReturnValue") // 约定：不覆写即视为「无附加量」
    default double defenseBonus(Player victim, DamageSource source, DamageCategory category,
            CombatStage stage) {
        return 0.0;
    }
}
