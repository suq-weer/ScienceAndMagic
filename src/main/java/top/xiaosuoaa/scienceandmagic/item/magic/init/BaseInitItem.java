package top.xiaosuoaa.scienceandmagic.item.magic.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.basic.components.ElementComponentRecord;

import java.util.List;

public class BaseInitItem extends Item {
	public BaseInitItem(List<String> arrayList) {
		super(new Properties()
				.rarity(Rarity.UNCOMMON)
				.stacksTo(1)
				.component(NeoModRegister.ELEMENT_COMPONENT.get(), new ElementComponentRecord(arrayList))
		);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack pStack, @NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
		pTooltipComponents.add(1, Component.translatable("tooltip.science_and_magic.init_item").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
	}
}
