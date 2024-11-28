package top.xiaosuoaa.scienceandmagic.basic.capability;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;

public record PlayerCapabilityData(int ep) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<PlayerCapabilityData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "player_data"));

	public static final StreamCodec<ByteBuf, PlayerCapabilityData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PlayerCapabilityData::ep,
			PlayerCapabilityData::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
