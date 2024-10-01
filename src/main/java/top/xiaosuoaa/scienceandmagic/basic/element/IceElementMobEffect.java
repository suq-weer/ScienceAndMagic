package top.xiaosuoaa.scienceandmagic.basic.element;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class IceElementMobEffect extends AbstractNatureElementMobEffect{
	public IceElementMobEffect(MobEffectCategory pCategory, int pColor) {
		super(pCategory, pColor);
	}

	@Override
	public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
		pLivingEntity.setTicksFrozen(600);
		return true;
	}
}
