package top.xiaosuoaa.scienceandmagic;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.xiaosuoaa.scienceandmagic.basic.capability.PlayerCapability;
import top.xiaosuoaa.scienceandmagic.basic.components.WeaponCategoryComponentRecord;
import top.xiaosuoaa.scienceandmagic.basic.creativetabs.ModCreativeModeTabs;
import top.xiaosuoaa.scienceandmagic.basic.element.*;
import top.xiaosuoaa.scienceandmagic.basic.components.ElementComponentRecord;
import top.xiaosuoaa.scienceandmagic.block.nature.sequoia.SequoiaLeavesBlock;
import top.xiaosuoaa.scienceandmagic.block.nature.sequoia.SequoiaLogBlock;
import top.xiaosuoaa.scienceandmagic.client.ClientModEvents;
import top.xiaosuoaa.scienceandmagic.client.gui.menu.PlayerCapabilityMenu;
import top.xiaosuoaa.scienceandmagic.entity.nature.sequoia.SequoiaBoat;
import top.xiaosuoaa.scienceandmagic.entity.nature.sequoia.SequoiaChestBoat;
import top.xiaosuoaa.scienceandmagic.item.magic.init.*;
import top.xiaosuoaa.scienceandmagic.item.nature.sequoia.SequoiaBoatItem;
import top.xiaosuoaa.scienceandmagic.item.nature.sequoia.SequoiaChestBoatItem;

import java.util.function.Supplier;

import static top.xiaosuoaa.scienceandmagic.basic.components.ElementComponentRecord.*;

public class NeoModRegister {
	//实体能力
	public static final EntityCapability<PlayerCapability,Void> PLAYER_CAPABILITY_HANDLER =
            EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID,"player_capability_handler"),
                    PlayerCapability.class);

	//树木类型
	//红杉木
	public static final BlockSetType SEQUOIA_BLOCK_SET_TYPE = new BlockSetType("sequoia");
	public static final WoodType SEQUOIA_WOOD_TYPE = new WoodType("sequoia", SEQUOIA_BLOCK_SET_TYPE);

	//药水效果
	public static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(Registries.MOB_EFFECT, ScienceAndMagic.MOD_ID);
	//元素效果
	public static final Supplier<MobEffect> ELEMENT_FIRE = MOB_EFFECT.register("element_fire", ()->new FireElementMobEffect(MobEffectCategory.NEUTRAL, 0xcc0202));
	public static final Supplier<MobEffect> ELEMENT_ICE = MOB_EFFECT.register("element_ice", ()->new IceElementMobEffect(MobEffectCategory.NEUTRAL, 0x47c9ed));
	public static final Supplier<MobEffect> ELEMENT_WOOD = MOB_EFFECT.register("element_wood", ()->new WoodElementMobEffect(MobEffectCategory.NEUTRAL, 0x22a759)
			.addAttributeModifier(
                Attributes.MOVEMENT_SPEED, ResourceLocation.withDefaultNamespace("effect.slowness"), -0.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
	);
	public static final Supplier<MobEffect> ELEMENT_STONE = MOB_EFFECT.register("element_stone", ()->new StoneElementMobEffect(MobEffectCategory.NEUTRAL, 0x22a759)
			.addAttributeModifier(
                Attributes.MOVEMENT_SPEED, ResourceLocation.withDefaultNamespace("effect.slowness"), -0.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
	);
	public static final Supplier<MobEffect> ELEMENT_LIGHTING = MOB_EFFECT.register("element_lighting", ()->new LightingElementMobEffect(MobEffectCategory.NEUTRAL, 0x3b3075));
	public static final Supplier<MobEffect> ELEMENT_WATER = MOB_EFFECT.register("element_water", ()->new WaterElementMobEffect(MobEffectCategory.NEUTRAL, 0x474163));

	//自定义数据组件
	public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(ScienceAndMagic.MOD_ID);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ElementComponentRecord>> ELEMENT_COMPONENT = COMPONENTS.registerComponentType(
			"element_component", builder -> builder
					.persistent(ELEMENT_COMPONENT_RECORD_CODEC)
					.networkSynchronized(ELEMENT_COMPONENT_RECORD_STREAM_CODEC)
	);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponCategoryComponentRecord>> WEAPON_CATEGORY_COMPONENT = COMPONENTS.registerComponentType(
			"weapon_category_component", builder -> builder
					.persistent(WeaponCategoryComponentRecord.WEAPON_CATEGORY_COMPONENT_RECORD_CODEC)
					.networkSynchronized(WeaponCategoryComponentRecord.WEAPON_CATEGORY_COMPONENT_RECORD_STREAM_CODEC)
	);


	//方块
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ScienceAndMagic.MOD_ID);
	public static final DeferredRegister.Items BLOCKITEMS = DeferredRegister.createItems(ScienceAndMagic.MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ScienceAndMagic.MOD_ID);
	//红杉木衍生物
	public static final Supplier<Block> SEQUOIA_LOG = BLOCKS.register(
			"sequoia_log",
			()->new SequoiaLogBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE)
					.instrument(NoteBlockInstrument.BASS)
					.strength(2.0F)
					.sound(SoundType.WOOD)
					.ignitedByLava()));
	public static final DeferredItem<BlockItem> SEQUOIA_LOG_ITEM = BLOCKITEMS.registerSimpleBlockItem("sequoia_log", SEQUOIA_LOG);
	public static final Supplier<Block> SEQUOIA_PLANK = BLOCKS.register(
			"sequoia_plank",
			()->new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_ORANGE)
					.instrument(NoteBlockInstrument.BASS)
					.strength(2.0F, 3.0F)
					.sound(SoundType.WOOD)
					.ignitedByLava()));
	public static final DeferredItem<BlockItem> SEQUOIA_PLANK_ITEM = BLOCKITEMS.registerSimpleBlockItem("sequoia_plank", SEQUOIA_PLANK);
	public static final Supplier<Block> PEELED_SEQUOIA_LOG = BLOCKS.register(
			"peeled_sequoia_log",
			()->new SequoiaLogBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_ORANGE)
					.instrument(NoteBlockInstrument.BASS)
					.strength(2.0F)
					.sound(SoundType.WOOD)
					.ignitedByLava()));
	public static final DeferredItem<BlockItem> PEELED_SEQUOIA_LOG_ITEM = BLOCKITEMS.registerSimpleBlockItem("peeled_sequoia_log", PEELED_SEQUOIA_LOG);
	public static final Supplier<CeilingHangingSignBlock> DARK_OAK_HANGING_SIGN = BLOCKS.register(
			"sequoia_hanging_sign",
			()->new CeilingHangingSignBlock(
					SEQUOIA_WOOD_TYPE,
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.COLOR_ORANGE)
							.forceSolidOn()
							.instrument(NoteBlockInstrument.BASS)
							.noCollission()
							.strength(1.0F)
							.ignitedByLava()
			)
	);
	public static final Supplier<Block> SEQUOIA_LEAVES = BLOCKS.register("sequoia_leaves", SequoiaLeavesBlock::new);


	//物品
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ScienceAndMagic.MOD_ID);
	//红杉木衍生物
	public static final Supplier<Item> SEQUOIA_BOAT_ITEM = ITEMS.register("sequoia_boat", ()->new SequoiaBoatItem(new Item.Properties()));
	public static final Supplier<Item> SEQUOIA_CHEST_BOAT_ITEM = ITEMS.register("sequoia_chest_boat", ()->new SequoiaChestBoatItem(new Item.Properties()));
	//魔法符文
	public static final Supplier<Item> FIRE_INIT_ITEM = ITEMS.register("fire_init", FireInitItem::new);
	public static final Supplier<Item> WATER_INIT_ITEM = ITEMS.register("water_init", WaterInitItem::new);
	public static final Supplier<Item> ICE_INIT_ITEM = ITEMS.register("ice_init", IceInitItem::new);
	public static final Supplier<Item> WOOD_INIT_ITEM = ITEMS.register("wood_init", WoodInitItem::new);
	public static final Supplier<Item> STONE_INIT_ITEM = ITEMS.register("stone_init", StoneInitItem::new);
	public static final Supplier<Item> LIGHTING_INIT_ITEM = ITEMS.register("lighting_init", LightingInitItem::new);

	//实体类型
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ScienceAndMagic.MOD_ID);
	//船
	public static final Supplier<EntityType<SequoiaBoat>> SEQUOIA_BOAT = ENTITY_TYPES.register("sequoia_boat", () -> EntityType.Builder.of(SequoiaBoat::new, MobCategory.MISC).sized(2, 0.5F).build("sequoia_boat"));
	public static final Supplier<EntityType<SequoiaChestBoat>> SEQUOIA_CHEST_BOAT = ENTITY_TYPES.register("sequoia_chest_boat", () -> EntityType.Builder.of(SequoiaChestBoat::new, MobCategory.MISC).sized(2, 0.5F).build("sequoia_chest_boat"));

	// Screens/Menus
	public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(Registries.MENU, ScienceAndMagic.MOD_ID);
	public static final Supplier<MenuType<PlayerCapabilityMenu>> PLAYER_CAPABILITY_GUI = MENU_TYPE.register("player_capability_gui", () -> IMenuTypeExtension.create((windowId, inv, data) -> new PlayerCapabilityMenu(windowId, inv)));

	/**
	 * <p>自定义物品等内容的注册方法。
	 * <p>注：自定义创造选项卡详见 {@link ModCreativeModeTabs}，自定义实体渲染器详见{@link ClientModEvents#onClientSetup(FMLClientSetupEvent)}
	 */
	public static void register(IEventBus eventBus) {
		MOB_EFFECT.register(eventBus);
		COMPONENTS.register(eventBus);
		BLOCKS.register(eventBus);
		BLOCKITEMS.register(eventBus);
		ITEMS.register(eventBus);
		ENTITY_TYPES.register(eventBus);
		MENU_TYPE.register(eventBus);
	}
}