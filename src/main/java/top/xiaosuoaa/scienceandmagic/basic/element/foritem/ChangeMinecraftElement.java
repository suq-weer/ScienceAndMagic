package top.xiaosuoaa.scienceandmagic.basic.element.foritem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.basic.components.ElementComponentRecord;

import java.util.ArrayList;
import java.util.List;

public class ChangeMinecraftElement {
	public static void changeElement(ModifyDefaultComponentsEvent event) {
		List<String> list = new ArrayList<>();
		list.add(ElementComponentRecord.STONE);
		list.add(ElementComponentRecord.FIRE);
		list.add(ElementComponentRecord.ICE);
		list.add(ElementComponentRecord.WOOD);
		list.add(ElementComponentRecord.WATER);
		list.add(ElementComponentRecord.LIGHTING);
		event.modify(Items.STONE_SWORD, builder ->
		    builder.set(NeoModRegister.ELEMENT_COMPONENT.value(), new ElementComponentRecord(list))
		);
	}

	public static void changeMinecraftTooltip(ItemTooltipEvent event) {
		ItemStack itemStack = event.getItemStack();
		if (itemStack.getItem().components().has(NeoModRegister.ELEMENT_COMPONENT.value())) {
			ElementComponentRecord components = itemStack.getItem().components().get(NeoModRegister.ELEMENT_COMPONENT.value());
			if (components != null) {
				ElementComponentRecord.elementTranslateCom(components.element(), event.getToolTip());
			}
		}
	}
}
