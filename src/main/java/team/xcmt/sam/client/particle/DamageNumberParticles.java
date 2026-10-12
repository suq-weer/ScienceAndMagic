// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.client.particle;

import net.minecraft.client.particle.ParticleRenderType;

/**
 * 伤害数字粒子的「渲染分组」常量。
 * <p>
 * 26.2 的 {@link ParticleRenderType} 已从「贴图表枚举」变成一条记录，
 * {@code ParticleEngine} 会按它把粒子分到对应的 {@code ParticleGroup}；
 * 没有注册工厂的类型会退化成 {@code QuadParticleGroup}（只接受贴图粒子，会类型转换失败），
 * 所以这个类型必须在 {@code RegisterParticleGroupsEvent} 里注册 {@link DamageNumberParticleGroup} 工厂。
 */
public final class DamageNumberParticles {

    /** 伤害数字的渲染分组（名字只用于性能分析段落与调试显示）。 */
    public static final ParticleRenderType RENDER_TYPE =
            new ParticleRenderType("SAM_DAMAGE_NUMBER", "SAMDN");

    private DamageNumberParticles() {
    }
}
