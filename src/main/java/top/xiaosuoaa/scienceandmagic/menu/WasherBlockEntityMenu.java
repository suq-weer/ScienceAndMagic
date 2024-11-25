package top.xiaosuoaa.scienceandmagic.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.block.crafter.WasherBlockEntity;

public class WasherBlockEntityMenu extends AbstractContainerMenu {
	private final ContainerData data;

	public WasherBlockEntityMenu(int pContainerId, Inventory inv, FriendlyByteBuf buf) {
		this(pContainerId, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(3));
	}

	public WasherBlockEntityMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
		super(NeoModRegister.WASHER_BLOCK_ENTITY_MENU.get(), pContainerId);
		checkContainerSize(inv, 3);
		this.data = data;
		WasherBlockEntity washerBlockEntity = (WasherBlockEntity) entity;
		ItemStackHandler itemStackHandler = washerBlockEntity.getItemStackHandler();
		this.addSlot(new SlotItemHandler(itemStackHandler, 0, 7, 8));
		this.addSlot(new SlotItemHandler(itemStackHandler, 1, 43, 44));
		this.addSlot(new SlotItemHandler(itemStackHandler, 2, 115, 44));
		addDataSlots(data);
		layoutPlayerInventorySlots(inv);
	}

	private void layoutPlayerInventorySlots(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

	//Shift 逻辑
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(pIndex);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (pIndex < 3) {
				if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
				if (pIndex < 3 + 27) {
					if (!this.moveItemStackTo(itemstack1, 3 + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, 3, 3 + 27, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;
			slot.onTake(pPlayer, itemstack1);
		}
		return itemstack;
	}

	@Override
	public boolean stillValid(@NotNull Player pPlayer) {
		return true;
	}

	public ContainerData getData() {
		return data;
	}
}
