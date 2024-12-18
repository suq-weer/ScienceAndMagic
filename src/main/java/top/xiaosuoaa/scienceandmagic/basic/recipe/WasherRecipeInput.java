package top.xiaosuoaa.scienceandmagic.basic.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public record WasherRecipeInput(ArrayList<ItemStack> itemStackList) implements RecipeInput {
	private static final int SIZE = 2;

	@Override
	public @NotNull ItemStack getItem(int pIndex) {
		if (pIndex > SIZE) throw new IllegalArgumentException("No item for index " + pIndex);
		return itemStackList.get(pIndex);
	}

	@Override
	public int size() {
		return SIZE;
	}
}
