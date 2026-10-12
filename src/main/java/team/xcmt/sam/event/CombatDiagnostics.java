// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.event;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.content.CombatAttributes;
import team.xcmt.sam.util.CombatDebug;

/**
 * 战斗系统的启动期侦测：玩家登录时打印一遍 12 项属性，便于直接核对「白板面板」。
 * <p>
 * 白板（未穿任何装备 / 无任何效果）应为：
 * bATK=2、bDEF=0、BLD=50、BLDk=5、pATK=mATK=sATK=0、pDEF=mDEF=0、mEP=0、PE=MC=100。
 */
@EventBusSubscriber(modid = ScienceAndMagic.MOD_ID)
public final class CombatDiagnostics {

    private CombatDiagnostics() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!CombatDebug.isEnabled()) {
            return;
        }
        Player player = event.getEntity();
        CombatDebug.log("玩家登录 {} 属性快照: bATK={} bDEF={} BLD={}% BLDk={} "
                + "pATK={}% mATK={}% sATK={}% pDEF={}% mDEF={}% mEP={} PE={}% MC={}%",
                player.getName().getString(),
                value(player, CombatAttributes.BASE_ATTACK),
                value(player, CombatAttributes.BASE_DEFEND),
                value(player, CombatAttributes.BLOOD_COUNTER),
                value(player, CombatAttributes.BLOOD_COUNT),
                value(player, CombatAttributes.PHYSIC_ATTACK),
                value(player, CombatAttributes.MAGIC_ATTACK),
                value(player, CombatAttributes.SCIENCE_ATTACK),
                value(player, CombatAttributes.PHYSIC_DEFEND),
                value(player, CombatAttributes.MAGIC_DEFEND),
                value(player, CombatAttributes.MAX_EP),
                value(player, CombatAttributes.PLAYER_ENERGY),
                value(player, CombatAttributes.MAGIC_COUNT));
    }

    private static String value(LivingEntity entity, Holder<Attribute> attribute) {
        return String.format("%.2f", entity.getAttributeValue(attribute));
    }
}
