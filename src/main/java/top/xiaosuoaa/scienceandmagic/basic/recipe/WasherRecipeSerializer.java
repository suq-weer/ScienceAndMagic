package top.xiaosuoaa.scienceandmagic.basic.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class WasherRecipeSerializer implements RecipeSerializer<WasherRecipe> {
	public static final MapCodec<WasherRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(WasherRecipe::getIngredient),
			ItemStack.CODEC.fieldOf("result").forGetter(WasherRecipe::getResult)
	).apply(instance, WasherRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, WasherRecipe> STREAM_CODEC =
			StreamCodec.composite(
					Ingredient.CONTENTS_STREAM_CODEC, WasherRecipe::getIngredient,
					ItemStack.STREAM_CODEC, WasherRecipe::getResult,
					WasherRecipe::new
			);

	@Override
	public @NotNull MapCodec<WasherRecipe> codec() {
		return CODEC;
	}

	@Override
	public @NotNull StreamCodec<RegistryFriendlyByteBuf, WasherRecipe> streamCodec() {
		return STREAM_CODEC;
	}
}
