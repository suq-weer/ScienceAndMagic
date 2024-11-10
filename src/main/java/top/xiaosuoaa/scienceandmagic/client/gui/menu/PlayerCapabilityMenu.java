package top.xiaosuoaa.scienceandmagic.client.gui.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlayerCapabilityMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	public final Level world;
	public final Player entity;
	private final Map<Integer, Slot> customSlots = new HashMap<>();

	public PlayerCapabilityMenu(int id, Inventory inv) {
		super(NeoModRegister.PLAYER_CAPABILITY_GUI.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
		return ItemStack.EMPTY;
	}

	public Map<Integer, Slot> get() {
		return customSlots;
	}
}

