package top.xiaosuoaa.scienceandmagic.item.magic.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.basic.components.ElementComponentRecord;

import java.util.List;

public class BaseInitItem extends Item implements GeoItem {
	private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

	public BaseInitItem(List<String> arrayList) {
		super(new Properties()
				.rarity(Rarity.UNCOMMON)
				.stacksTo(1)
				.component(NeoModRegister.ELEMENT_COMPONENT.get(), new ElementComponentRecord(arrayList))
		);
	}

	private <T extends BaseInitItem> PlayState playState(AnimationState<T> animationState) {
		animationState.getController().setAnimation(RawAnimation.begin().thenLoop("animation.init.loop"));
		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "controller", 0, this::playState));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack pStack, @NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
		pTooltipComponents.add(1, Component.translatable("tooltip.science_and_magic.init_item").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
	}
}
