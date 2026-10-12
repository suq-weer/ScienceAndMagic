package team.xcmt.sam;

import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(ScienceAndMagic.MOD_ID)
@EventBusSubscriber(Dist.DEDICATED_SERVER)
public class ScienceAndMagic {
    public static final String MOD_ID = "science_and_magic";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 本模组命名空间下的标识符，等价于 {@code Identifier.fromNamespaceAndPath(MOD_ID, path)}。 */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    // ModContainer 是 NeoForge 的构造注入形参：本模组暂时用不到，但保留它可让注入签名保持完整。
    @SuppressWarnings("unused")
    public ScienceAndMagic(IEventBus modEventBus, ModContainer modContainer) {
        Register.init(modEventBus);
    }
}
