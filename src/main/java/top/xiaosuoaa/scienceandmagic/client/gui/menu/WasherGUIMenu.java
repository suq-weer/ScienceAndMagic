package top.xiaosuoaa.scienceandmagic.client.gui.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeType;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;

public class WasherGUIMenu extends AbstractFurnaceMenu {
	public WasherGUIMenu(int pContainerId, Inventory pPlayerInventory) {
		super(NeoModRegister.WASHER_GUI.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, pContainerId, pPlayerInventory);
	}
}