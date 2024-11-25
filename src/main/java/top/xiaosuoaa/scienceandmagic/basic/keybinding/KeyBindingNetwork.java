package top.xiaosuoaa.scienceandmagic.basic.keybinding;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;
import top.xiaosuoaa.scienceandmagic.menu.PlayerCapabilityMenu;

public record KeyBindingNetwork(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<KeyBindingNetwork> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "key_player_capability_key"));
	public static final StreamCodec<RegistryFriendlyByteBuf, KeyBindingNetwork> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, KeyBindingNetwork message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new KeyBindingNetwork(buffer.readInt(), buffer.readInt()));

	@Override
	public @NotNull Type<KeyBindingNetwork> type() {
		return TYPE;
	}

	public static void handleData(final KeyBindingNetwork message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> pressAction(context.player(), message.eventType)).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	public static void pressAction(Player entity, int type) {
		if (type == 0) {
			entity.openMenu(new SimpleMenuProvider(
					(pContainerId, pPlayerInventory, pPlayer) ->  new PlayerCapabilityMenu(pContainerId, pPlayerInventory),
					Component.translatable("gui.science_and_magic.player_capability_gui.label_player_capability_gui")
			));
		}
	}
}

