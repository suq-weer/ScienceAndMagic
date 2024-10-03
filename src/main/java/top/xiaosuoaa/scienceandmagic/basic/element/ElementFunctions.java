package top.xiaosuoaa.scienceandmagic.basic.element;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.Collection;

public class ElementFunctions {
	public static int scanElementRestrainFIW(MobEffect effect1, MobEffect effect2) {
		if (effect1 instanceof FireElementMobEffect && effect2 instanceof IceElementMobEffect) {
			return 0;
		}
		if (effect1 instanceof IceElementMobEffect && effect2 instanceof WaterElementMobEffect) {
			return 1;
		}
		if (effect1 instanceof WaterElementMobEffect && effect2 instanceof FireElementMobEffect) {
			return 2;
		}
		return -1;
	}

	public static void scanElement(MobEffectEvent.Added event) {
		MobEffectInstance willAddEffect = event.getEffectInstance();
		LivingEntity eventEntity = event.getEntity();
		Collection<MobEffectInstance> activeEffects = eventEntity.getActiveEffects();
		for (int i = 0; i < activeEffects.size(); i++) {
			MobEffectInstance effectInstance = (MobEffectInstance) activeEffects.toArray()[i];
			if (willAddEffect != null && ElementFunctions.scanElementRestrainFIW(willAddEffect.getEffect().value(), effectInstance.getEffect().value()) != -1) {
				eventEntity.removeEffect(effectInstance.getEffect());
			}
		}
	}
}
