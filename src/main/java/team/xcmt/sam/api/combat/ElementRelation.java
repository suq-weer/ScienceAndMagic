// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.combat;

import javax.annotation.Nullable;

/**
 * 元素克制关系表（纯函数，无运行时依赖）。
 * <p>
 * 规则（由手写克制图确认）：
 * <ul>
 *     <li><b>克制环</b>：火 → 冰 → 水 → 火。克制方输出 ×1.5、被克制方输出 ×0.5（按元素对结算，与谁先出手无关）。</li>
 *     <li><b>自克</b>：木 / 石 / 电 同元素互攻为<b>增伤</b>——攻击方输出 ×1.5。</li>
 *     <li><b>减免</b>：木 ⇄ 石 互相减伤，双方输出 ×0.5。</li>
 *     <li>其余组合 ×1.0。</li>
 * </ul>
 * 倍率统一作用于「攻击方的输出伤害」，因此只需一个方向即可表达双向增减。
 */
public final class ElementRelation {

    /** 克制方输出倍率。 */
    public static final double COUNTER = 1.5;

    /** 被克制方输出倍率。 */
    public static final double COUNTERED = 0.5;

    /** 木 ⇄ 石的互相减伤倍率。 */
    public static final double MUTUAL_REDUCE = 0.5;

    private ElementRelation() {
    }

    /** {@code attacker} 是否克制 {@code defender}。 */
    public static boolean counters(ElementType attacker, ElementType defender) {
        return (attacker == ElementType.FIRE && defender == ElementType.ICE)
                || (attacker == ElementType.ICE && defender == ElementType.WATER)
                || (attacker == ElementType.WATER && defender == ElementType.FIRE);
    }

    /** 木 ⇄ 石的互相减免。 */
    public static boolean mutualReduce(ElementType a, ElementType b) {
        return (a == ElementType.WOOD && b == ElementType.STONE)
                || (a == ElementType.STONE && b == ElementType.WOOD);
    }

    /** 该元素是否具有「自克」（同元素互攻增伤）。 */
    public static boolean selfCounter(ElementType element) {
        return element == ElementType.WOOD || element == ElementType.STONE
                || element == ElementType.LIGHTING;
    }

    /**
     * 攻方元素对守方元素的输出伤害倍率。
     *
     * @param attacker 攻方元素，{@code null} 表示无元素
     * @param defender 守方元素，{@code null} 表示无元素
     * @return 1.0 / 1.5 / 0.5
     */
    public static double multiplier(@Nullable ElementType attacker, @Nullable ElementType defender) {
        if (attacker == null || defender == null) {
            return 1.0;
        }
        if (counters(attacker, defender)) {
            return COUNTER;
        }
        if (counters(defender, attacker)) {
            return COUNTERED;
        }
        if (attacker == defender && selfCounter(attacker)) {
            // 自克：同元素互攻为增伤
            return COUNTER;
        }
        if (mutualReduce(attacker, defender)) {
            return MUTUAL_REDUCE;
        }
        return 1.0;
    }

    /**
     * 该元素对是否产生了实际关系（用于判断是否需要让双方元素强度各降一级）。
     */
    public static boolean consumes(@Nullable ElementType attacker, @Nullable ElementType defender) {
        return attacker != null && defender != null && multiplier(attacker, defender) != 1.0;
    }
}
