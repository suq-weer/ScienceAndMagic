package top.xiaosuoaa.scienceandmagic.client;

import net.minecraft.client.color.block.BlockColor;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import top.xiaosuoaa.scienceandmagic.NeoModRegister;

public class ModBlockAndItemColor {
	public static final BlockColor SEQUOIA_LEAVES_COLOR = (state, reader, pos, tintIndex) -> 0xffb95a;

	public static void registerBlockColor(RegisterColorHandlersEvent.Block event) {
		event.register(SEQUOIA_LEAVES_COLOR, NeoModRegister.SEQUOIA_LEAVES.get());
	}
}
