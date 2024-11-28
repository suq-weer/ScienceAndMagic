package top.xiaosuoaa.scienceandmagic.block.crafter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;
import top.xiaosuoaa.scienceandmagic.menu.WasherBlockEntityMenu;

public class WasherBlockEntity extends BlockEntity implements MenuProvider {

	//容器数据
	protected final ContainerData data;

	//容器大小/数据存放地
	private int stackSize = 3;
	private final ItemStackHandler itemStackHandler = new ItemStackHandler(stackSize);

	//构建函数->新建容器数据 data
	public WasherBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(NeoModRegister.WASHER_BLOCK_ENTITY.get(), pPos, pBlockState);
		this.data = new ContainerData() {
			@Override
			public int get(int pIndex) {
				if (pIndex == 0) {
					return stackSize;
				}
				return 0;
			}

			@Override
			public void set(int pIndex, int pValue) {
				if (pIndex == 0) {
					WasherBlockEntity.this.stackSize = pValue;
				}
			}

			@Override
			public int getCount() {
				return 3;
			}
		};
	}

	/**
	 * 每 Tick 服务端逻辑。
	 *
	 * @param pLevel       {@link Level}
	 * @param pPos         {@link BlockPos}
	 * @param pState       {@link BlockState}
	 * @param pBlockEntity {@link WasherBlockEntity}
	 */
	public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, WasherBlockEntity pBlockEntity) {
		if (pLevel != null && !pLevel.isClientSide) {
			for (int i = 0; i < pBlockEntity.stackSize; i++) {
				pBlockEntity.data.set(i, pBlockEntity.itemStackHandler.getStackInSlot(i).getCount());
			}
		}
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("gui." + ScienceAndMagic.MOD_ID + ".crafter_washer_gui");
	}

	//创建容器
	@Override
	public @Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
		return new WasherBlockEntityMenu(pContainerId, pPlayerInventory, this, this.data);
	}

	//数据读取/保存
	@Override
	protected void loadAdditional(@NotNull CompoundTag pTag, HolderLookup.@NotNull Provider pRegistries) {
		super.loadAdditional(pTag, pRegistries);
		this.itemStackHandler.deserializeNBT(pRegistries, pTag);
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag pTag, HolderLookup.@NotNull Provider pRegistries) {
		super.saveAdditional(pTag, pRegistries);
		pTag.put("inventory", this.itemStackHandler.serializeNBT(pRegistries));
	}

	//容器数据 getter
	public ItemStackHandler getItemStackHandler() {
		return this.itemStackHandler;
	}
}
