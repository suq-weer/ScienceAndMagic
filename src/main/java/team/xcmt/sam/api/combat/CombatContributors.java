// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.combat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * {@link CombatContributor} 的注册表。
 * <p>
 * 当前没有任何实现：武器 / 魂骨 / 装备的数值都由物品自身的属性修饰符表达，
 * 这里只作为后续「非属性类附加结算」的挂载点。
 */
public final class CombatContributors {

    private static final List<CombatContributor> CONTRIBUTORS = new CopyOnWriteArrayList<>();

    private CombatContributors() {
    }

    /** 注册一个贡献者。 */
    public static void register(CombatContributor contributor) {
        CONTRIBUTORS.add(contributor);
    }

    /** 汇总某一攻击阶段的附加伤害。 */
    public static double attackBonus(Player attacker, LivingEntity target, DamageCategory category,
            CombatStage stage) {
        double sum = 0.0;
        for (CombatContributor contributor : CONTRIBUTORS) {
            sum += contributor.attackBonus(attacker, target, category, stage);
        }
        return sum;
    }

    /** 汇总某一防御阶段的附加防御。 */
    public static double defenseBonus(Player victim, DamageSource source, DamageCategory category,
            CombatStage stage) {
        double sum = 0.0;
        for (CombatContributor contributor : CONTRIBUTORS) {
            sum += contributor.defenseBonus(victim, source, category, stage);
        }
        return sum;
    }
}
