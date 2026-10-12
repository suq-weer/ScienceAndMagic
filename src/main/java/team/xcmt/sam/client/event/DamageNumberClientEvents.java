// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.client.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import team.xcmt.sam.client.particle.DamageNumberParticleGroup;
import team.xcmt.sam.client.particle.DamageNumberParticles;
import team.xcmt.sam.client.particle.DamageNumberProvider;
import team.xcmt.sam.content.ModParticles;

/**
 * 伤害数字粒子的客户端注册（都在 mod 总线、仅客户端）。
 * <p>
 * 两件事缺一不可：
 * <ul>
 *     <li>{@link RegisterParticleProvidersEvent}：把「粒子类型 → 粒子实例」的工厂挂上，
 *         没有它客户端收得到生成包却<b>造不出粒子</b>；</li>
 *     <li>{@link RegisterParticleGroupsEvent}：把「渲染分组 → 粒子组」的工厂挂上，
 *         没有它 {@code ParticleEngine} 会退化成只认贴图粒子的 {@code QuadParticleGroup}，
 *         我们的文字粒子会被当成贴图粒子强转而报错。</li>
 * </ul>
 */
@EventBusSubscriber(Dist.CLIENT)
public final class DamageNumberClientEvents {

    private DamageNumberClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticles.DAMAGE_NUMBER.get(), new DamageNumberProvider());
    }

    @SubscribeEvent
    public static void onRegisterParticleGroups(RegisterParticleGroupsEvent event) {
        event.register(DamageNumberParticles.RENDER_TYPE, DamageNumberParticleGroup::new);
    }
}
