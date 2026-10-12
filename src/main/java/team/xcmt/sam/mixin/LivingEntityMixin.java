// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import team.xcmt.sam.content.CombatPipeline;
import team.xcmt.sam.util.CombatDebug;

/**
 * 玩家受击接管。
 * <p>
 * 在 {@code getDamageAfterArmorAbsorb} 处替换原版护甲公式：仅当受击者是玩家时，改由
 * {@link CombatPipeline#computeDefense} 按「减法式最终防御」计算并取消原方法；非玩家实体
 * 完全不受影响。
 * <p>
 * 兼容适配层：{@code SamDefenseEvent} 被取消时 {@code computeDefense} 返回 {@code null}，
 * 此处便不取消原方法，回退到原版护甲公式。
 * <p>
 * 其后的 {@code getDamageAfterMagicAbsorb}（抗性提升、附魔保护等原版效果）保持原样，
 * 对应机制文档里防御流程的「第 7 步 原版效果」。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    // Mixin 惯用法：本类不被仪器化为 LivingEntity 的子类，(Object) this 相关的类型判断
    // 在编译期看起来恒为假，只有注入后才有意义，因此关闭这一族的常量/类型流检查。
    @SuppressWarnings("ConstantConditions")
    private void sam$replaceArmorAbsorb(DamageSource source, float damage,
            CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player) {
            CombatPipeline.DefenseResult result = CombatPipeline.computeDefense(player, source, damage);
            if (result.damage() != null) {
                cir.setReturnValue(result.damage());
            }
        } else if (CombatDebug.isEnabled() && source.getEntity() instanceof Player) {
            // 玩家打非玩家目标：保留原版护甲公式，便于与攻击日志对照
            CombatDebug.log("防御钩子: {} 非玩家 → 保留原版护甲公式（伤害 {}）",
                    ((LivingEntity) (Object) this).getName().getString(), damage);
        }
    }
}
