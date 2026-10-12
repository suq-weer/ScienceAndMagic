package team.xcmt.sam.api.combat.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.api.combat.DamageCategory;

/**
 * 兼容适配层：玩家受击时、本模组用「减法式最终防御」替换原版护甲公式之前触发。
 * <p>
 * 其他模组可以：
 * <ul>
 *     <li>{@link #setDefense(double)} 修改最终防御（减法式中的减数）；</li>
 *     <li>{@link #setDamage(double)} 直接覆写最终受到的伤害；</li>
 *     <li>{@link #setCategory(DamageCategory)} 改写伤害归属；</li>
 *     <li>取消事件表示<b>放弃</b>本模组的减伤，回退到原版护甲公式。</li>
 * </ul>
 * 事件只在服务端触发（受击结算本身只在服务端进行）。
 */
public class SamDefenseEvent extends LivingEvent implements ICancellableEvent {

    private final DamageSource damageSource;
    private DamageCategory category;
    private final double incoming;
    private double defense;
    private double damage;

    public SamDefenseEvent(Player victim, DamageSource damageSource, DamageCategory category,
            double incoming, double defense, double damage) {
        super(victim);
        this.damageSource = damageSource;
        this.category = category;
        this.incoming = incoming;
        this.defense = defense;
        this.damage = damage;
    }

    /** 受击玩家。 */
    @Override
    public @NonNull Player getEntity() {
        return (Player) super.getEntity();
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

    /** 接收到的原始伤害。 */
    public double getIncoming() {
        return this.incoming;
    }

    /** 最终防御（减数）。 */
    public double getDefense() {
        return this.defense;
    }

    public void setDefense(double defense) {
        this.defense = Math.max(0.0, defense);
    }

    /** 最终受到的伤害。 */
    public double getDamage() {
        return this.damage;
    }

    public void setDamage(double damage) {
        this.damage = Math.max(0.0, damage);
    }
}
