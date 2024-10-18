package top.xiaosuoaa.scienceandmagic;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.slf4j.Logger;
import top.xiaosuoaa.scienceandmagic.nature.resource.SequoiaBoatRenderer;
import top.xiaosuoaa.scienceandmagic.nature.resource.SequoiaChestBoatRenderer;

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
}