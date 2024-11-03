package top.xiaosuoaa.scienceandmagic;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import top.xiaosuoaa.scienceandmagic.basic.creativetabs.ModCreativeModeTabs;
import top.xiaosuoaa.scienceandmagic.basic.element.ElementFunctions;
import top.xiaosuoaa.scienceandmagic.basic.magic.EPEnergy;
import top.xiaosuoaa.scienceandmagic.basic.magic.EPEnergyData;
import top.xiaosuoaa.scienceandmagic.basic.magic.EPEnergyProvider;

import java.util.Optional;

@GameTestHolder(ScienceAndMagic.MOD_ID)
@Mod(ScienceAndMagic.MOD_ID)
public class ScienceAndMagic {
	public static final String MOD_ID = "science_and_magic";
	private static final Logger LOGGER = LogUtils.getLogger();

	public ScienceAndMagic(IEventBus modEventBus)
	{
		info("开始监听……");
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::registerCapabilities);
		modEventBus.addListener(this::register);
		NeoForge.EVENT_BUS.addListener(this::onPlayerJoinWorld);
		NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
		ModCreativeModeTabs.register(modEventBus);
		NeoModRegister.register(modEventBus);
		NeoForge.EVENT_BUS.register(this);
		info("监听完成！");
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
		if (event.getSource().is(DamageTypes.MOB_ATTACK)
				|| event.getSource().is(DamageTypes.PLAYER_ATTACK)
				|| event.getSource().is(DamageTypes.MOB_ATTACK)) {
			LivingEntity entity = (LivingEntity) event.getSource().getEntity();
			LivingEntity hurtEntity = event.getEntity();
			if (entity != null) {
				ItemStack itemStack = entity.getItemInHand(InteractionHand.MAIN_HAND);
				ElementFunctions.itemGiveEntityElement(itemStack, hurtEntity, event);
			}
		}
	}

	private void onPlayerTick(PlayerTickEvent.Post event) {
		if(!event.getEntity().level().isClientSide()) {
			Optional<EPEnergy> optionalPlayerThirst = Optional.ofNullable(event.getEntity().getCapability(NeoModRegister.PLAYER_EP_HANDLER));
			optionalPlayerThirst .ifPresent(epEnergy -> {
				if(epEnergy.getEnergyStored() < epEnergy.getMaxEnergyStored()) {
					epEnergy.receiveEnergy(1, false);
				}
				if (epEnergy.getEnergyStored() > epEnergy.getMaxEnergyStored()) {
					epEnergy.setEp(epEnergy.getMaxEnergyStored());
				}
				PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new EPEnergyData(epEnergy.getEnergyStored()));
			});
		}
	}

	private void onPlayerJoinWorld(EntityJoinLevelEvent event) {
		if(!event.getLevel().isClientSide()) {
			if(event.getEntity() instanceof ServerPlayer player) {
				EPEnergy capability = player.getCapability(NeoModRegister.PLAYER_EP_HANDLER);
				if (capability != null && capability.getEnergyStored() == 0) {
					capability.setMaxEp(100);
				}
				Optional<EPEnergy> epEnergy1 = Optional.ofNullable(capability);
				epEnergy1.ifPresent(epEnergy -> PacketDistributor.sendToPlayer(player, new EPEnergyData(epEnergy.getEnergyStored())));
			}
		}
	}

	private void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(HandlerThread.NETWORK.name());
        registrar.playBidirectional(
            EPEnergyData.TYPE,
            EPEnergyData.STREAM_CODEC,
            new DirectionalPayloadHandler<>(
		            (epEnergyData, context) -> {
			            Minecraft minecraft = Minecraft.getInstance();
			            LocalPlayer player = minecraft.player;
			            if(player != null) {
				            EPEnergy capability = player.getCapability(NeoModRegister.PLAYER_EP_HANDLER);
				            if (capability != null) {
					            capability.setEp(epEnergyData.ep());
				            }
			            }
		            },
		            (epEnergyData, context) -> {}
            )
        );
	}

	private void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerEntity(NeoModRegister.PLAYER_EP_HANDLER,
				EntityType.PLAYER,
				new EPEnergyProvider());
	}

	public static void info(String s) {
		LOGGER.info(s);
	}
	public static void warn(String s) {
		LOGGER.warn(s);
	}
}