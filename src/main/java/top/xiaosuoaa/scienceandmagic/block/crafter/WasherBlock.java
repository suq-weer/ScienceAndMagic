package top.xiaosuoaa.scienceandmagic.block.crafter;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.xiaosuoaa.scienceandmagic.client.gui.menu.WasherGUIMenu;

public class WasherBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty IS_WORKING = BooleanProperty.create("is_working");

	WasherBlockEntity washerBlockEntity;

	public WasherBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK));
	}

	@Override
	public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
		registerDefaultState(stateDefinition.any().setValue(FACING, pContext.getHorizontalDirection()));
		return super.getStateForPlacement(pContext);
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos) {
		BlockEntity tileEntity = pLevel.getBlockEntity(pPos);
		return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
	}

	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull BlockHitResult pHitResult) {
		if (!pLevel.isClientSide && pPlayer instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(
					new SimpleMenuProvider(
							WasherGUIMenu::new,
							Component.translatable("block.science_and_magic.washer.tile")
					),
					pPos);
        }

        return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder);
		pBuilder.add(FACING);
		pBuilder.add(IS_WORKING);
	}

	@Override
	protected int getAnalogOutputSignal(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos) {
		BlockEntity tileentity = pLevel.getBlockEntity(pPos);
		if (tileentity instanceof WasherBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else
			return 0;

	}

	@Override
	protected boolean triggerEvent(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, int pId, int pParam) {
		super.triggerEvent(pState, pLevel, pPos, pId, pParam);
		BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
		return blockEntity != null && blockEntity.triggerEvent(pId, pParam);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		washerBlockEntity = new WasherBlockEntity(pPos, pState);
		return washerBlockEntity;
	}
}
