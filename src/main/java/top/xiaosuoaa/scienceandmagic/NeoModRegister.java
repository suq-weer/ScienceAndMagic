package top.xiaosuoaa.scienceandmagic;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.xiaosuoaa.scienceandmagic.basic.element.*;

import java.util.function.Supplier;

public class NeoModRegister {
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

	public static void register(IEventBus eventBus) {
		MOB_EFFECT.register(eventBus);
	}
}