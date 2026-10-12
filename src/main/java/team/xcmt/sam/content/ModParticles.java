// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.content;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.content.particle.DamageNumberParticleOptions;

/**
 * 模组自带的粒子类型。
 * <p>
 * 仅有 {@code science_and_magic:damage_number}（伤害数字）。它是一个<b>带数据</b>的粒子
 * （{@link DamageNumberParticleOptions}），所以注册成匿名的 {@link ParticleType} 子类，
 * 由参数自己提供序列化器；参数里带 {@code damage}，因此同一个类型就能画出任意数值。
 * <p>
 * {@code overrideLimiter = true} 与「伤害指示器」{@code minecraft:damage_indicator} 一致：
 * 让客户端在「粒子效果：最少」设置下、以及超过 32 格距离剔除时<b>依然生成</b>，
 * 否则伤害数字会时有时无。
 */
public final class ModParticles {

    /** 粒子类型注册表。 */
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ScienceAndMagic.MOD_ID);

    /** 伤害数字粒子。 */
    public static final DeferredHolder<ParticleType<?>, ParticleType<DamageNumberParticleOptions>> DAMAGE_NUMBER =
            PARTICLE_TYPES.register("damage_number", () -> new ParticleType<>(true) {
                @Override
                public @NonNull MapCodec<DamageNumberParticleOptions> codec() {
                    return DamageNumberParticleOptions.CODEC;
                }

                @Override
                public @NonNull StreamCodec<? super RegistryFriendlyByteBuf, DamageNumberParticleOptions> streamCodec() {
                    return DamageNumberParticleOptions.STREAM_CODEC;
                }
            });

    private ModParticles() {
    }
}
