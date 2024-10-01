package top.xiaosuoaa.scienceandmagic;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(ScienceAndMagic.MOD_ID)
public class ScienceAndMagic {
	public static final String MOD_ID = "science_and_magic";
	private static final Logger LOGGER = LogUtils.getLogger();

	public ScienceAndMagic(IEventBus modEventBus)
	{
		modEventBus.addListener(this::commonSetup);
		NeoModRegister.register(modEventBus);
		NeoForge.EVENT_BUS.register(this);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("服务端正在启动……");
	}

	public static void info(String s) {
		LOGGER.info(s);
	}
}