package top.xiaosuoaa.scienceandmagic.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;
import top.xiaosuoaa.scienceandmagic.datagen.recipe.WasherRecipeProvider;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ScienceAndMagic.MOD_ID, value = Dist.DEDICATED_SERVER)
public class GatherData {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(event.includeServer(), new WasherRecipeProvider(output, lookupProvider));
	}
}
