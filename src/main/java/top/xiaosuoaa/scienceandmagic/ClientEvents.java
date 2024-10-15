package top.xiaosuoaa.scienceandmagic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static top.xiaosuoaa.scienceandmagic.ScienceAndMagic.MOD_ID;
import static top.xiaosuoaa.scienceandmagic.basic.element.foritem.ChangeMinecraftElement.changeMinecraftTooltip;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
		changeMinecraftTooltip(event);
	}
}
