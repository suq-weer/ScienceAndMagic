package top.xiaosuoaa.scienceandmagic.basic.element;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AbstractNatureElementMobEffect extends MobEffect {

	protected AbstractNatureElementMobEffect(MobEffectCategory pCategory, int pColor) {
		super(pCategory, pColor);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
		return pDuration % 20 == 0;
	}
}
