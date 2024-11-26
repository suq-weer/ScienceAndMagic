package top.xiaosuoaa.scienceandmagic.datagen.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class WasherRecipe implements Recipe<WasherRecipeInput> {
	private final ArrayList<ItemStack> itemStackList;
	private final ItemStack result;

	public WasherRecipe(ArrayList<ItemStack> pItemStackList, ItemStack pResult) {
		itemStackList = pItemStackList;
		result = pResult;
	}

	@Override
	public boolean matches(@NotNull WasherRecipeInput pInput, @NotNull Level pLevel) {
		return this.itemStackList.containsAll(pInput.itemStackList());
	}

	@Override
	public @NotNull ItemStack assemble(WasherRecipeInput pInput, HolderLookup.Provider pRegistries) {
		return this.result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return true;
	}

	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider pRegistries) {
		return this.result.copy();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return null;
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		return null;
	}

	public ArrayList<ItemStack> getItemStackList() {
		return itemStackList;
	}

	public ItemStack getResult() {
		return result;
	}
}
