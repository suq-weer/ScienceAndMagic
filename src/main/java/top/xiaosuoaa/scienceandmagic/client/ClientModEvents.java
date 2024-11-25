package top.xiaosuoaa.scienceandmagic.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.slf4j.Logger;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;
import top.xiaosuoaa.scienceandmagic.basic.keybinding.KeyBinding;
import top.xiaosuoaa.scienceandmagic.client.screen.PlayerCapabilityScreen;
import top.xiaosuoaa.scienceandmagic.client.screen.WasherBlockEntityScreen;
import top.xiaosuoaa.scienceandmagic.entity.nature.sequoia.SequoiaBoatRenderer;
import top.xiaosuoaa.scienceandmagic.entity.nature.sequoia.SequoiaChestBoatRenderer;

import static top.xiaosuoaa.scienceandmagic.ScienceAndMagic.MOD_ID;
import static top.xiaosuoaa.scienceandmagic.basic.element.foritem.ChangeMinecraftElement.changeElement;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
	private static final Logger LOGGER = LogUtils.getLogger();

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		LOGGER.info("客户端正在启动……");
		LOGGER.info("Minecraft 登录账号：{}", Minecraft.getInstance().getUser().getName());
		event.enqueueWork(()->{
			EntityRenderers.register(NeoModRegister.SEQUOIA_BOAT.get(), (EntityRendererProvider.Context pContext) -> new SequoiaBoatRenderer(pContext, false));
			EntityRenderers.register(NeoModRegister.SEQUOIA_CHEST_BOAT.get(), (EntityRendererProvider.Context pContext) -> new SequoiaChestBoatRenderer(pContext, true));
		});
	}

	@SubscribeEvent
	public static void modifyComponents(ModifyDefaultComponentsEvent event) {
		changeElement(event);
	}

	@SubscribeEvent
	public static void registerBlockColor(RegisterColorHandlersEvent.Block event) {
		ModBlockAndItemColor.registerBlockColor(event);
	}

	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(NeoModRegister.PLAYER_CAPABILITY_GUI.get(), PlayerCapabilityScreen::new);
		event.register(NeoModRegister.WASHER_BLOCK_ENTITY_MENU.get(), WasherBlockEntityScreen::new);
	}

	@SubscribeEvent
	public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.PLAYER_CAPABILITY_KEY);
    }
}