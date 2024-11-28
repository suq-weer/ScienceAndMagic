package top.xiaosuoaa.scienceandmagic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.basic.capability.PlayerCapability;
import top.xiaosuoaa.scienceandmagic.basic.keybinding.KeyBinding;
import top.xiaosuoaa.scienceandmagic.basic.keybinding.KeyBindingNetwork;

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
		PlayerCapability capability = null;
		if (player != null) {
			capability = player.getCapability(NeoModRegister.PLAYER_CAPABILITY_HANDLER);
		}
		if (capability != null) {
			int maxX = 120;
			double maxEPX = maxX * ((double) capability.getEp() / (double) capability.getMEp());
			int aH = guiGraphics.guiHeight() - 10;
			guiGraphics.fill(5, aH, maxX + 5, aH + 5, new Color(0, 0, 0, 0.5f).getRGB());
			guiGraphics.fill(5, aH, (int) maxEPX + 5, aH + 5, new Color(0x66bb33).getRGB());
			guiGraphics.drawString(
					minecraft.font,
					Component.translatable("gui.science_and_magic.ep").append(capability.getEp() + "/" + capability.getMEp()),
					10,
					aH - 5,
					Color.WHITE.getRGB());
		}
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event) {
		if (KeyBinding.PLAYER_CAPABILITY_KEY.consumeClick()) {
			PacketDistributor.sendToServer(new KeyBindingNetwork(0, 0));
		}
	}
}
