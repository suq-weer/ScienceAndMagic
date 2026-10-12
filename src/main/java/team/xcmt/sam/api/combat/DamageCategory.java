// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.combat;

/**
 * 一次伤害的归属类别，决定走哪一套加成 / 防御属性。
 * <ul>
 *     <li>{@link #PHYSICAL}：物理，吃 pATK / pDEF / PE；</li>
 *     <li>{@link #MAGIC}：法术，吃 mATK / mDEF / MC；</li>
 *     <li>{@link #TECH}：科技，吃 sATK（本轮仅归类，不参与结算）。</li>
 * </ul>
 */
public enum DamageCategory {
    PHYSICAL,
    MAGIC,
    TECH
}
