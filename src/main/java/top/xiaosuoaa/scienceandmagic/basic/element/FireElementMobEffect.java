package top.xiaosuoaa.scienceandmagic.basic.element;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class FireElementMobEffect extends AbstractNatureElementMobEffect {
	public FireElementMobEffect(MobEffectCategory pCategory, int pColor) {
		super(pCategory, pColor);
	}

	@Override
	public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
		if (!pLivingEntity.level().isClientSide()) {
			pLivingEntity.hurt(pLivingEntity.level().damageSources().inFire(), 1.0F);
		}
		return true;
	}
}