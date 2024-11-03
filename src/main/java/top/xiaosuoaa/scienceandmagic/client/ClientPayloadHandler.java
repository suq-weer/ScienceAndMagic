package top.xiaosuoaa.scienceandmagic.client;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.xiaosuoaa.scienceandmagic.basic.magic.EPEnergyData;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ClientPayloadHandler {
    private static final Logger LOGGER = (Logger) LogUtils.getLogger();
    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }
    public void handleThirstData(final EPEnergyData data, final IPayloadContext context) {
        CompletableFuture.runAsync(() -> {
            try {
                ClientPlayerEPData.set(data.ep());
            } catch (Exception e) {
                LOGGER.severe("Failed to set thirst data: " + e.getMessage());
                context.channelHandlerContext().disconnect((ChannelPromise) Component.translatable("my_mod.networking.failed", e.getMessage()));
            }
        });
    }

}

