package top.xiaosuoaa.scienceandmagic.nature.resource;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class SequoiaLeavesBlock extends LeavesBlock {
	public SequoiaLeavesBlock() {
		super(
				BlockBehaviour.Properties.of()
						.mapColor(MapColor.PLANT)
						.strength(0.2F)
						.randomTicks()
						.sound(SoundType.GRASS)
						.noOcclusion()
						.isValidSpawn(Blocks::ocelotOrParrot)
						.isSuffocating((pState, pLevel, pPos) -> false)
						.isViewBlocking(((pState, pLevel, pPos) -> false))
						.ignitedByLava()
						.pushReaction(PushReaction.DESTROY)
						.isRedstoneConductor((pState, pLevel, pPos) -> false)
		);
	}
}
