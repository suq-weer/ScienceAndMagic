// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.content.particle.DamageNumberParticleOptions;

/**
 * 伤害数字粒子的客户端工厂：把网络同步过来的 {@link DamageNumberParticleOptions} 变成粒子实例。
 */
public final class DamageNumberProvider implements ParticleProvider<DamageNumberParticleOptions> {

    @Override
    public Particle createParticle(@NonNull DamageNumberParticleOptions options, @NonNull ClientLevel level,
            double x, double y, double z, double xAux, double yAux, double zAux, @NonNull RandomSource random) {
        return new DamageNumberParticle(level, x, y, z, options.damage(), options.color(), options.scale(), random);
    }
}
