package top.xiaosuoaa.scienceandmagic.basic.element;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import org.jetbrains.annotations.NotNull;

public class WoodElementMobEffect extends AbstractNatureElementMobEffect {
	private BlockPos lastPos;

	public WoodElementMobEffect(MobEffectCategory pCategory, int pColor) {
		super(pCategory, pColor);
		lastPos = new BlockPos(0, 0, 0);
	}

	@Override
	public boolean applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
		if (pLivingEntity.level().isClientSide()) {
			return true;
		}
		ServerLevel eLevel = (ServerLevel) pLivingEntity.level();
		BlockPos pos = new BlockPos(pLivingEntity.getBlockX(), ((int) pLivingEntity.getBoundingBox().minY) - 1, pLivingEntity.getBlockZ());
		if (!pos.equals(lastPos)) {
			lastPos = pos;
			Block block = eLevel.getBlockState(pos).getBlock();
			if (block instanceof BonemealableBlock bonemealableBlock) {
				bonemealableBlock.performBonemeal(eLevel, RandomSource.create(), pos, eLevel.getBlockState(pos));
			}
		}
		return true;
	}
}