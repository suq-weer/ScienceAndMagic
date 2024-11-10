package top.xiaosuoaa.scienceandmagic.basic.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
	public static final String KEY_CATEGORY_SCIENCE_AND_MAGIC = Component.translatable("key.category.science_and_magic").getString();
    public static final String KEY_OPEN_PLAYER_CAPABILITY = Component.translatable("key.science_and_magic.open_player_capability").getString();
    public static final KeyMapping PLAYER_CAPABILITY_KEY = new KeyMapping(KEY_OPEN_PLAYER_CAPABILITY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY_SCIENCE_AND_MAGIC);
}
