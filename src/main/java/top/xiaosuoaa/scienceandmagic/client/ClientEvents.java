package top.xiaosuoaa.scienceandmagic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.basic.magic.EPEnergy;

import java.awt.*;

import static top.xiaosuoaa.scienceandmagic.ScienceAndMagic.MOD_ID;
import static top.xiaosuoaa.scienceandmagic.basic.element.foritem.ChangeMinecraftElement.changeMinecraftTooltip;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
		changeMinecraftTooltip(event);
	}

	@SubscribeEvent
	public static void initGuiOverlay(RenderGuiLayerEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		GuiGraphics guiGraphics = event.getGuiGraphics();
		EPEnergy capability = null;
		if (player != null) {
			capability = player.getCapability(NeoModRegister.PLAYER_EP_HANDLER);
		}
		if (capability != null) {
			int maxX = 120;
			double maxEPX = maxX*((double) capability.getEnergyStored() / (double) capability.getMaxEnergyStored());
			int aH = guiGraphics.guiHeight() - 5;
			guiGraphics.fill(5, aH, maxX+5, aH+5, new Color(0,0,0, 0.5f).getRGB());
			guiGraphics.fill(5, aH, (int) maxEPX+5, aH+5, new Color(0x66bb33).getRGB());
			guiGraphics.drawString(
					minecraft.font,
					Component.translatable("gui.science_and_magic.ep").append(capability.getEnergyStored()+"/"+capability.getMaxEnergyStored()),
					10,
					aH-5,
					Color.WHITE.getRGB());
		}
	}
}
