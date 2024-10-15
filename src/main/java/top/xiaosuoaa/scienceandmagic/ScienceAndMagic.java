package top.xiaosuoaa.scienceandmagic;

import com.mojang.logging.LogUtils;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import top.xiaosuoaa.scienceandmagic.basic.creativetabs.ModCreativeModeTabs;
import top.xiaosuoaa.scienceandmagic.basic.element.ElementFunctions;

@Mod(ScienceAndMagic.MOD_ID)
public class ScienceAndMagic {
	public static final String MOD_ID = "science_and_magic";
	private static final Logger LOGGER = LogUtils.getLogger();

	public ScienceAndMagic(IEventBus modEventBus)
	{
		info("开始监听……");
		modEventBus.addListener(this::commonSetup);
		ModCreativeModeTabs.register(modEventBus);
		NeoModRegister.register(modEventBus);
		NeoForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	private void scanEffect(MobEffectEvent.Added event) {
		ElementFunctions.scanElementEffect(event);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		 warn("此版本为早期开发版本，可能会有极度破坏游戏的问题发生！");
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("服务端正在启动……");
	}

	@SubscribeEvent
	public void onLivingAttack(LivingIncomingDamageEvent event) {
		if (event.getSource().is(DamageTypes.MOB_ATTACK)) {
			LivingEntity entity = (LivingEntity) event.getSource().getEntity();
			LivingEntity hurtEntity = event.getEntity();
			if (entity != null) {
				ItemStack itemStack = entity.getUseItem();
				ElementFunctions.itemGiveElement(itemStack, hurtEntity);
			}
		}
	}

	public static void info(String s) {
		LOGGER.info(s);
	}
	public static void warn(String s) {
		LOGGER.warn(s);
	}
}