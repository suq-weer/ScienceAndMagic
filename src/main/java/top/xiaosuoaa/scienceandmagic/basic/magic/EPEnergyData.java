package top.xiaosuoaa.scienceandmagic.basic.magic;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.scienceandmagic.ScienceAndMagic;

public record EPEnergyData(int ep) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<EPEnergyData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, "ep_data"));

	public static final StreamCodec<ByteBuf, EPEnergyData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        EPEnergyData::ep,
        EPEnergyData::new
    );

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
