package top.xiaosuoaa.scienceandmagic.basic.creativetabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
	private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			ScienceAndMagic.MOD_ID
	);
	public static final Supplier<CreativeModeTab> SCIENCE_AND_MAGIC;
	static {
		SCIENCE_AND_MAGIC = CREATIVE_MODE_TABS.register(ScienceAndMagic.MOD_ID, () -> CreativeModeTab.builder()
				.withTabsBefore(CreativeModeTabs.COMBAT)
				.title(Component.translatable("itemGroup." + ScienceAndMagic.MOD_ID + ".science_and_magic"))
				.icon(() -> NeoModRegister.SEQUOIA_LOG_ITEM.get().getDefaultInstance())
				.displayItems((params, output) -> {
					//红杉木衍生物
					output.accept(NeoModRegister.SEQUOIA_LOG_ITEM);
					output.accept(NeoModRegister.SEQUOIA_PLANK_ITEM);
					output.accept(NeoModRegister.PEELED_SEQUOIA_LOG_ITEM);
					output.accept(NeoModRegister.SEQUOIA_BOAT_ITEM.get());
					output.accept(NeoModRegister.SEQUOIA_CHEST_BOAT_ITEM.get());
					//魔法符文
					output.accept(NeoModRegister.ICE_INIT_ITEM.get());
					output.accept(NeoModRegister.FIRE_INIT_ITEM.get());
					output.accept(NeoModRegister.WATER_INIT_ITEM.get());
					output.accept(NeoModRegister.WOOD_INIT_ITEM.get());
					output.accept(NeoModRegister.LIGHTING_INIT_ITEM.get());
					output.accept(NeoModRegister.STONE_INIT_ITEM.get());

					ScienceAndMagic.info("已构建模组全物品创造选项卡");
				})
				.build()
		);
	}

	public static void register(IEventBus eventBus) {
		CREATIVE_MODE_TABS.register(eventBus);
	}
}
