package team.xcmt.sam.client.gui;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * 玩家属性面板的打开入口。
 * <p>
 * 面板是纯客户端界面：12 项属性都带 {@code setSyncable(true)}，客户端本地已有权威数值，
 * 因此无需 Menu 与服务端同步，直接用 {@link ModularUI#of(UI)}（无玩家上下文的重载）。
 * 背景使用 LDLib2 自带的 modern 主题（{@link StylesheetManager#MODERN}）。
 */
public final class PlayerCapabilityScreen {

    private static final String TITLE_KEY = "gui.science_and_magic.player_capability_gui.label_player_capability_gui";

    private PlayerCapabilityScreen() {
    }

    /**
     * 打开玩家属性面板。
     */
    public static void open() {
        UI ui = UI.of(new PlayerCapabilityPanel(),
                StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MODERN));
        // 26.2 的 Minecraft 不再提供 setScreen(Screen)，原版各处统一走 Gui#setScreen；
        // 这里也不使用 setScreenAndShow，避免在按钮点击（渲染中）时强制补帧。
        Minecraft.getInstance().gui.setScreen(new ModularUIScreen(ModularUI.of(ui), Component.translatable(TITLE_KEY)));
    }
}
