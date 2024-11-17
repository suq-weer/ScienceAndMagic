package top.xiaosuoaa.scienceandmagic.block.crafter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;

public class WasherBlockEntity extends BlockEntity implements Container {
	private final NonNullList<ItemStack> items = NonNullList.withSize(
            3,
            ItemStack.EMPTY
    );

	public WasherBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(NeoModRegister.WASHER_BLOCK_ENTITY.get(), pPos, pBlockState);
	}

	@Override
	public int getContainerSize() {
		return 3;
	}
	@Override
	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}
	@Override
	public @NotNull ItemStack getItem(int pSlot) {
		return this.items.get(pSlot);
	}
	@Override
	public @NotNull ItemStack removeItem(int pSlot, int pAmount) {
		ItemStack stack = ContainerHelper.removeItem(this.items, pSlot, pAmount);
        this.setChanged();
        return stack;
	}
	@Override
	public @NotNull ItemStack removeItemNoUpdate(int pSlot) {
		ItemStack stack = ContainerHelper.takeItem(this.items, pSlot);
        this.setChanged();
        return stack;
	}
	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		pStack.limitSize(this.getMaxStackSize(pStack));
        this.items.set(pSlot, pStack);
        this.setChanged();
	}
	@Override
	public boolean stillValid(@NotNull Player pPlayer) {
		return true;
	}
	@Override
	public void clearContent() {
		items.clear();
        this.setChanged();
	}
}
