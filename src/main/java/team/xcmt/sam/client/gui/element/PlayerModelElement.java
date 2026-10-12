// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.client.gui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.IGUIContext;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

import org.jspecify.annotations.NonNull;

/**
 * 面向鼠标的玩家模型元素。
 * <p>
 * LDLib2 没有可直接渲染实体的内置元素（其 {@code Scene} 只能渲染方块世界），因此这里用一个
 * 空白 {@link UIElement} 占位，并在 {@link #drawBackgroundAdditional(IGUIContext)} 里桥接到原版
 * 物品栏使用的 {@link InventoryScreen#extractEntityInInventoryFollowsMouse}，
 * 从而得到与原版物品栏一致的「模型随鼠标转头」效果。
 * <p>
 * 绘制使用元素的<b>绝对</b>坐标（{@code getPositionX/Y}）与尺寸（{@code getSizeWidth/Height}），
 * 因为绘制发生在 LDLib2 已经推入的面板坐标系中，绝对坐标即为该坐标系下的位置。
 */
public class PlayerModelElement extends UIElement {

    /** 原版物品栏渲染玩家模型时使用的尺寸。 */
    private static final float DEFAULT_SCALE = 30.0F;
    /** 原版物品栏渲染玩家模型时使用的垂直偏移。 */
    private static final float DEFAULT_OFFSET_Y = 0.0625F;

    private float scale = DEFAULT_SCALE;
    private float offsetY = DEFAULT_OFFSET_Y;

    public PlayerModelElement() {
    }

    /**
     * 调整模型渲染尺寸（对应原版 {@code extractEntityInInventoryFollowsMouse} 的 {@code size} 参数）。
     */
    public PlayerModelElement scale(float scale) {
        this.scale = scale;
        return this;
    }

    /**
     * 调整模型垂直偏移（对应原版同名方法的 {@code offsetY} 参数）。
     */
    public PlayerModelElement offsetY(float offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    @Override
    protected void drawBackgroundAdditional(@NonNull IGUIContext context) {
        if (!(context instanceof GUIContext guiContext)) {
            return;
        }
        LivingEntity player = guiContext.mc.player;
        if (player == null) {
            return;
        }
        float width = getSizeWidth();
        float height = getSizeHeight();
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }
        float x = getPositionX();
        float y = getPositionY();
        // 元素坐标与 localMouse 处在同一坐标系（根元素的 getLayoutX/Y 就是 ModularUI 的 leftPos/topPos），
        // 因此必须传 localMouse，而不是屏幕原始鼠标坐标，否则模型朝向会整体偏掉。
        InventoryScreen.extractEntityInInventoryFollowsMouse(guiContext.graphics,
                Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height),
                Math.round(this.scale), this.offsetY,
                Math.round(guiContext.localMouseX), Math.round(guiContext.localMouseY), player);
    }
}
