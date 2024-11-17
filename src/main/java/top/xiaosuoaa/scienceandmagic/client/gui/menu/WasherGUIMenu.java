package top.xiaosuoaa.scienceandmagic.client.gui.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.block.crafter.WasherBlockEntity;
import top.xiaosuoaa.scienceandmagic.item.magic.init.BaseInitItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WasherGUIMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	public final Level world;
	private final ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private boolean bound = false;
	private final WasherBlockEntity boundBlockEntity;
	private final Inventory inventory;

	public WasherGUIMenu(int id, Inventory inv, Player extraData) {
		super(NeoModRegister.WASHER_GUI.get(), id);
		this.inventory = inv;
		this.world = inv.player.level();
		this.internal = new ItemStackHandler(3);
		BlockPos pos = extraData.getOnPos();
		boundBlockEntity = (WasherBlockEntity) this.world.getBlockEntity(pos);
		if (boundBlockEntity instanceof WasherBlockEntity baseContainerBlockEntity) {
			this.internal = new InvWrapper(baseContainerBlockEntity);
			this.bound = true;
		}
		this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 7, 8) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.getItem() instanceof BaseInitItem;
			}
		}));
		this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 43, 44) {
		}));
		this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 115, 44) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return false;
			}
		}));
		if (boundBlockEntity != null && !boundBlockEntity.getItem(0).isEmpty()) {
			this.customSlots.get(0).set(boundBlockEntity.getItem(0).copy());
		}
		if (boundBlockEntity != null && !boundBlockEntity.getItem(1).isEmpty()) {
			this.customSlots.get(1).set(boundBlockEntity.getItem(1).copy());
		}
		if (boundBlockEntity != null && !boundBlockEntity.getItem(2).isEmpty()) {
			this.customSlots.get(2).set(boundBlockEntity.getItem(2).copy());
		}
		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 8 + si * 18, 142));
	}
	public WasherGUIMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(NeoModRegister.WASHER_GUI.get(), id);
		this.inventory = inv;
		this.world = inv.player.level();
		this.internal = new ItemStackHandler(3);
		BlockPos pos = extraData.readBlockPos();
		boundBlockEntity = (WasherBlockEntity) this.world.getBlockEntity(pos);
		if (boundBlockEntity instanceof WasherBlockEntity baseContainerBlockEntity) {
			this.internal = new InvWrapper(baseContainerBlockEntity);
			this.bound = true;
		}
		this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 7, 8) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.getItem() instanceof BaseInitItem;
			}
		}));
		this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 43, 44) {
		}));
		this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 115, 44) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return false;
			}
		}));
		if (boundBlockEntity != null && !boundBlockEntity.getItem(0).isEmpty()) {
			this.customSlots.get(0).set(boundBlockEntity.getItem(0).copy());
		}
		if (boundBlockEntity != null && !boundBlockEntity.getItem(1).isEmpty()) {
			this.customSlots.get(1).set(boundBlockEntity.getItem(1).copy());
		}
		if (boundBlockEntity != null && !boundBlockEntity.getItem(2).isEmpty()) {
			this.customSlots.get(2).set(boundBlockEntity.getItem(2).copy());
		}
		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 8 + si * 18, 142));
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (this.bound) {
			if (this.boundBlockEntity != null)
				return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
		}
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < 3) {
				if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
				if (index < 3 + 27) {
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
			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@Override
	public void slotsChanged(@NotNull Container pContainer) {
		super.slotsChanged(pContainer);
		for (int i = 0; i < 3; i++) {
			if (pContainer.getItem(i) != inventory.getItem(i)) {
				inventory.setItem(i, pContainer.getItem(i));
			}
		}
	}

	@Override
	protected boolean moveItemStackTo(@NotNull ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
		boolean flag = false;
		int i = pStartIndex;
		if (pReverseDirection) {
			i = pEndIndex - 1;
		}
		if (pStack.isStackable()) {
			while (!pStack.isEmpty() && (pReverseDirection ? i >= pStartIndex : i < pEndIndex)) {
				Slot slot = this.slots.get(i);
				ItemStack itemstack = slot.getItem();
				if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(pStack, itemstack)) {
					int j = itemstack.getCount() + pStack.getCount();
					int k = slot.getMaxStackSize(itemstack);
					if (j <= k) {
						pStack.setCount(0);
						itemstack.setCount(j);
						slot.set(itemstack);
						flag = true;
					} else if (itemstack.getCount() < k) {
						pStack.shrink(k - itemstack.getCount());
						itemstack.setCount(k);
						slot.set(itemstack);
						flag = true;
					}
				}
				if (pReverseDirection) {
					i--;
				} else {
					i++;
				}
			}
		}
		if (!pStack.isEmpty()) {
			if (pReverseDirection) {
				i = pEndIndex - 1;
			} else {
				i = pStartIndex;
			}
			while (pReverseDirection ? i >= pStartIndex : i < pEndIndex) {
				Slot slot1 = this.slots.get(i);
				ItemStack itemstack1 = slot1.getItem();
				if (itemstack1.isEmpty() && slot1.mayPlace(pStack)) {
					int l = slot1.getMaxStackSize(pStack);
					slot1.setByPlayer(pStack.split(Math.min(pStack.getCount(), l)));
					slot1.setChanged();
					flag = true;
					break;
				}
				if (pReverseDirection) {
					i--;
				} else {
					i++;
				}
			}
		}
		return flag;
	}

	@Override
	public void removed(@NotNull Player playerIn) {
		super.removed(playerIn);
		if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
				for (int j = 0; j < internal.getSlots(); ++j) {
					playerIn.drop(internal.getStackInSlot(j), false);
					if (internal instanceof IItemHandlerModifiable ihm)
						ihm.setStackInSlot(j, ItemStack.EMPTY);
				}
			} else {
				for (int i = 0; i < internal.getSlots(); ++i) {
					playerIn.getInventory().placeItemBackInInventory(internal.getStackInSlot(i));
					if (internal instanceof IItemHandlerModifiable ihm)
						ihm.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		}
	}

	public Map<Integer, Slot> get() {
		return customSlots;
	}
}

