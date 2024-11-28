package top.xiaosuoaa.scienceandmagic;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import top.xiaosuoaa.scienceandmagic.basic.capability.PlayerCapability;
import top.xiaosuoaa.scienceandmagic.basic.capability.PlayerCapabilityData;
import top.xiaosuoaa.scienceandmagic.basic.capability.PlayerCapabilityProvider;
import top.xiaosuoaa.scienceandmagic.basic.creativetabs.ModCreativeModeTabs;
import top.xiaosuoaa.scienceandmagic.basic.element.ElementFunctions;
import top.xiaosuoaa.scienceandmagic.basic.keybinding.KeyBindingNetwork;

import java.util.Objects;
import java.util.Optional;

@Mod(ScienceAndMagic.MOD_ID)
public class ScienceAndMagic {
	public static final String MOD_ID = "science_and_magic";
	private static final Logger LOGGER = LogUtils.getLogger();

	public ScienceAndMagic(IEventBus modEventBus) {
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

	private static float playerAttackSync(LivingDamageEvent.Pre event) {
		Entity eventEntity = event.getSource().getEntity();
		if (eventEntity instanceof Player && eventEntity.getCapability(NeoModRegister.PLAYER_CAPABILITY_HANDLER) != null) {
			DamageSource eventSource = event.getSource();
			ItemStack weaponItem = eventSource.getWeaponItem();
			int weaponType = 0;
			if (weaponItem != null) {
				DataComponentMap weaponItemComponents = weaponItem.getComponents();
				if (weaponItemComponents.has(NeoModRegister.WEAPON_CATEGORY_COMPONENT.get())) {
					weaponType = Objects.requireNonNull(weaponItemComponents.get(NeoModRegister.WEAPON_CATEGORY_COMPONENT.get())).weaponCategory();
				}
				return PlayerCapability.useAttackCount((Player) eventEntity, weaponType, weaponItem.getMaxDamage());
			}
			return PlayerCapability.useAttackCount((Player) eventEntity, weaponType, 0);
		}
		return event.getNewDamage();
	}

	public static void info(String s) {
		LOGGER.info(s);
	}

	public static void warn(String s) {
		LOGGER.warn(s);
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

	@SubscribeEvent
	public void onPlayerAttack(LivingDamageEvent.Pre event) {
		float playerAttackSync = playerAttackSync(event);
		// ...
		info(String.valueOf(playerAttackSync));
		event.setNewDamage(playerAttackSync);
	}

	private void onPlayerTick(PlayerTickEvent.Post event) {
		if (!event.getEntity().level().isClientSide()) {
			Optional<PlayerCapability> optionalPlayerThirst = Optional.ofNullable(event.getEntity().getCapability(NeoModRegister.PLAYER_CAPABILITY_HANDLER));
			optionalPlayerThirst.ifPresent(playerCapability -> {
				if (playerCapability.getEp() < playerCapability.getMEp()) {
					playerCapability.addEp(1, false);
				}
				if (playerCapability.getEp() > playerCapability.getMEp()) {
					playerCapability.setEp(playerCapability.getMEp());
				}
				PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new PlayerCapabilityData(playerCapability.getEp()));
			});
		}
	}

	private void onPlayerJoinWorld(EntityJoinLevelEvent event) {
		if (!event.getLevel().isClientSide()) {
			if (event.getEntity() instanceof ServerPlayer player) {
				PlayerCapability capability = player.getCapability(NeoModRegister.PLAYER_CAPABILITY_HANDLER);
				if (capability != null && capability.getEp() == 0) {
					capability.setMEp(100);
				}
				Optional<PlayerCapability> epEnergy1 = Optional.ofNullable(capability);
				epEnergy1.ifPresent(playerCapability -> PacketDistributor.sendToPlayer(player, new PlayerCapabilityData(playerCapability.getEp())));
			}
		}
	}

	private void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(HandlerThread.NETWORK.name());
		registrar.playBidirectional(
				PlayerCapabilityData.TYPE,
				PlayerCapabilityData.STREAM_CODEC,
				new DirectionalPayloadHandler<>(
						(playerCapabilityData, context) -> {
							Minecraft minecraft = Minecraft.getInstance();
							LocalPlayer player = minecraft.player;
							if (player != null) {
								PlayerCapability capability = player.getCapability(NeoModRegister.PLAYER_CAPABILITY_HANDLER);
								if (capability != null) {
									capability.setEp(playerCapabilityData.ep());
								}
							}
						},
						(playerCapabilityData, context) -> {
						}
				)
		);
		registrar.playBidirectional(
				KeyBindingNetwork.TYPE,
				KeyBindingNetwork.STREAM_CODEC,
				new DirectionalPayloadHandler<>(
						(payload, context) -> {
						},
						KeyBindingNetwork::handleData
				)
		);
	}

	private void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerEntity(NeoModRegister.PLAYER_CAPABILITY_HANDLER,
				EntityType.PLAYER,
				new PlayerCapabilityProvider());
	}
}