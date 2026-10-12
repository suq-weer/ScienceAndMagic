// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.combat.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import team.xcmt.sam.api.combat.DamageCategory;

/**
 * 兼容适配层：本模组的玩家攻击在「发出伤害」之前触发该事件。
 * <p>
 * 其他模组可以通过它观察 / 修改新机制的结算结果：
 * <ul>
 *     <li>{@link #setDamage(double)} 修改最终伤害；</li>
 *     <li>{@link #setCategory(DamageCategory)} 改写伤害归属（物理 / 法术 / 科技）；</li>
 *     <li>{@link #setCritical(boolean)} 改写致命状态（影响表现）；</li>
 *     <li>取消事件（{@link ICancellableEvent}）表示本次攻击不造成伤害。</li>
 * </ul>
 * 事件在客户端与服务器都会触发；只有服务端的结果会被真正应用。
 * <p>
 * 另外，原版 {@code CriticalHitEvent} 依然会在每次攻击时触发（本模组把自身的致命判定作为
 * 「原版暴击状态」传入），因此依赖它的第三方逻辑可以继续工作。
 */
public class SamAttackEvent extends PlayerEvent implements ICancellableEvent {

    private final Entity target;
    private final DamageSource damageSource;
    private DamageCategory category;
    private double damage;
    private boolean critical;

    public SamAttackEvent(Player attacker, Entity target, DamageSource damageSource,
            DamageCategory category, double damage, boolean critical) {
        super(attacker);
        this.target = target;
        this.damageSource = damageSource;
        this.category = category;
        this.damage = damage;
        this.critical = critical;
    }

    /** 被攻击者。 */
    public Entity getTarget() {
        return this.target;
    }

    /** 伤害来源。 */
    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    /** 伤害归属类别。 */
    public DamageCategory getCategory() {
        return this.category;
    }

    public void setCategory(DamageCategory category) {
        this.category = category;
    }

    /** 最终伤害（已四舍五入）。 */
    public double getDamage() {
        return this.damage;
    }

    public void setDamage(double damage) {
        this.damage = Math.max(0.0, damage);
    }

    /** 是否触发致命。 */
    public boolean isCritical() {
        return this.critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }
}
