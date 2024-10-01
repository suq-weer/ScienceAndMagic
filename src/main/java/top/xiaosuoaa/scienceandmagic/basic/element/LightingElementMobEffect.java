package top.xiaosuoaa.scienceandmagic.basic.element;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class LightingElementMobEffect extends AbstractNatureElementMobEffect{
	public LightingElementMobEffect(MobEffectCategory pCategory, int pColor) {
		super(pCategory, pColor);
	}

	@Override
	public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if (!pLivingEntity.level().isClientSide()) {
			int pAmount = new Random().nextInt(7) + 1;
			pLivingEntity.hurt(pLivingEntity.level().damageSources().lightningBolt(), pAmount);
			return pAmount != 7;
		}
		return true;
	}
}
