package top.xiaosuoaa.scienceandmagic.basic.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;

import java.util.Optional;

public class WasherRecipe implements Recipe<WasherRecipeInput> {
	private final Ingredient ingredient;
	private final ItemStack result;

	public WasherRecipe(Ingredient ingredient, ItemStack pResult) {
		this.ingredient = ingredient;
		result = pResult;
	}

	public ItemStack getResult() {
		return result;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	@Override
	public boolean matches(@NotNull WasherRecipeInput pInput, @NotNull Level pLevel) {
		Optional<ItemStack> any = pInput.itemStackList().stream().findAny();
		return any.filter(this.ingredient).isPresent();
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull WasherRecipeInput pInput, HolderLookup.@NotNull Provider pRegistries) {
		return this.result.copy();
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nonNullList = NonNullList.create();
		nonNullList.add(ingredient);
		return nonNullList;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return true;
	}

	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider pRegistries) {
		return this.result;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return new WasherRecipeSerializer();
	}

	@Override
	public @NotNull RecipeType<?> getType() {
		return NeoModRegister.WASHER_RECIPE.get();
	}
}
