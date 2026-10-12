package team.xcmt.sam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

import team.xcmt.sam.content.CombatPipeline;
import team.xcmt.sam.util.CombatDebug;

/**
 * 玩家攻击接管。
 * <ul>
 *     <li>{@code canCriticalAttack} 恒返回 false —— 删除原版「跳劈暴击」；</li>
 *     <li>{@code CommonHooks.fireCriticalHit} 改为喂入本模组的致命判定（倍率默认 1.0），
 *         使原版 {@link CriticalHitEvent} 继续对第三方可用，同时不再产生 ×1.5 的原版加成；</li>
 *     <li>{@code hurtOrSimulate} 的实参改由 {@link CombatPipeline#computeAttack} 计算 ——
 *         原版 {@code ATTACK_DAMAGE} 属性、附魔加成、×1.5 暴击倍率因此全部失效。</li>
 * </ul>
 * 之所以用 {@code @Redirect} 而非在方法头整体接管，是为了完整保留原版的击退、横扫、音效、
 * 统计、饥饿消耗等副作用，只替换伤害数值与暴击判定。
 * <p>
 * {@code Player.attack} 在客户端也会被调用（用于表现预测），{@link CombatPipeline} 内部已区分端：
 * 客户端只读元素、不消耗强度。
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "canCriticalAttack", at = @At("HEAD"), cancellable = true)
    // 形参必须与目标方法 canCriticalAttack(Entity) 对齐，但判定恒为 false，无需读取 target。
    @SuppressWarnings("unused")
    private void sam$disableJumpCritical(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/common/CommonHooks;fireCriticalHit("
                    + "Lnet/minecraft/world/entity/player/Player;"
                    + "Lnet/minecraft/world/entity/Entity;ZF)"
                    + "Lnet/neoforged/neoforge/event/entity/player/CriticalHitEvent;"))
    private CriticalHitEvent sam$fireCriticalHit(Player player, Entity target, boolean vanillaCritical,
            float damageModifier) {
        boolean critical = CombatPipeline.beginAttack(player, target);
        CriticalHitEvent event = CommonHooks.fireCriticalHit(player, target, critical, 1.0F);
        CombatPipeline.applyCriticalEvent(event);
        return event;
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate("
                    + "Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    // Mixin 惯用法：本类不被仪器化为 Player 的子类，(Object) this 的强制转换编译期看似必然失败。
    @SuppressWarnings("ConstantConditions")
    private boolean sam$replaceAttackDamage(Entity target, DamageSource source, float vanillaDamage) {
        Player self = (Player) (Object) this;
        CombatPipeline.AttackResult result = CombatPipeline.computeAttack(self, target, source);
        if (CombatDebug.isEnabled()) {
            CombatDebug.log("攻击钩子: 原版伤害 {} 已丢弃，改发新机制伤害 {}（致命={}）",
                    vanillaDamage, result.damage(), result.critical());
        }
        if (result.critical()) {
            CombatPipeline.playCriticalEffects(self, target);
        }
        if (target.level() instanceof ServerLevel serverLevel) {
            return target.hurtServer(serverLevel, source, result.damage());
        }
        return target.hurtClient(source);
    }
}
