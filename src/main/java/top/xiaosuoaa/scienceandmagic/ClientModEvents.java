package top.xiaosuoaa.scienceandmagic;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

import static top.xiaosuoaa.scienceandmagic.ScienceAndMagic.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
	private static final Logger LOGGER = LogUtils.getLogger();

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		LOGGER.info("客户端正在启动……");
		LOGGER.info("Minecraft 版本：{}", Minecraft.getInstance().getUser().getName());
	}
}