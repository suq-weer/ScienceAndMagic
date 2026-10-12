package team.xcmt.sam.client.event;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import team.xcmt.sam.client.gui.PlayerCapabilityScreen;

/**
 * 在原版物品栏（玩家背包）界面底部挂一个「玩家属性」按钮，作为属性面板的唯一入口。
 * <p>
 * 覆盖两种物品栏：生存模式的 {@link InventoryScreen} 与创造模式的
 * {@link CreativeModeInventoryScreen}（后者是另一个类，不是 {@code InventoryScreen} 的子类，
 * 因此必须单独判断，否则创造模式下不会出现按钮）。
 * <p>
 * 使用 {@link ScreenEvent.Init.Post} 而非 Mixin：该事件通过 {@code addListener} 添加的监听器，
 * 若实现了 {@code Renderable}/{@code NarratableEntry} 会被自动计入屏幕的可渲染控件与旁白，
 * 而原版 {@link Button} 两者都实现了，因此按钮可以正常渲染与点击。
 * <p>
 * 位置由 {@code AbstractContainerScreen} 的公开方法 {@code getLeftPos/getTopPos/getImageWidth/getImageHeight}
 * 计算得出，无需访问器 Mixin。
 */
@EventBusSubscriber(Dist.CLIENT)
public final class InventoryUiEvents {

    /** 属性面板按钮文案键。 */
    private static final String BUTTON_KEY = "gui.science_and_magic.player_capability_gui.button_open";
    private static final int BUTTON_WIDTH = 92;
    private static final int BUTTON_HEIGHT = 16;
    /** 按钮与物品栏面板底边的间距。 */
    private static final int BUTTON_OFFSET_Y = 4;
    /**
     * 创造模式物品栏在面板<b>下方</b>还画了一排页签（高度 32），
     * 因此按钮要再往下让开这段距离，避免遮住页签。
     */
    private static final int CREATIVE_TABS_HEIGHT = 32;
    /** 至少为屏幕底部留出的边距，防止窗口过矮时按钮越界。 */
    private static final int SCREEN_BOTTOM_MARGIN = 4;

    private InventoryUiEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        boolean creative = screen instanceof CreativeModeInventoryScreen;
        if (!creative && !(screen instanceof InventoryScreen)) {
            return;
        }
        int x = screen.getLeftPos() + (screen.getImageWidth() - BUTTON_WIDTH) / 2;
        int y = screen.getTopPos() + screen.getImageHeight() + BUTTON_OFFSET_Y
                + (creative ? CREATIVE_TABS_HEIGHT : 0);
        y = Math.min(y, screen.height - BUTTON_HEIGHT - SCREEN_BOTTOM_MARGIN);
        Button button = Button.builder(Component.translatable(BUTTON_KEY), pressed -> PlayerCapabilityScreen.open())
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        event.addListener(button);
    }
}
