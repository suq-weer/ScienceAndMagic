// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;

import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import team.xcmt.sam.client.gui.element.PlayerModelElement;
import team.xcmt.sam.content.CombatAttributes;

/**
 * 玩家属性面板的根元素。
 * <p>
 * 采用 LDLib2 的 modern 主题（由 {@link PlayerCapabilityScreen} 在构建 {@code UI} 时挂载），
 * 布局为「上：玩家模型；下：两组并排的属性分组」；面板内以<b>字母缩写</b>显示属性名，
 * 悬浮任意一行显示该属性的中文译名。
 * <p>
 * 数值统一保留一位小数，百分比类属性（BLD、pATK、mATK、sATK、pDEF、mDEF、PE、MC）带 {@code %} 后缀。
 * 每 tick 通过 {@link #screenTick()} 读取一次玩家属性，仅在格式化结果变化时才写回文本，
 * 因此装备/魂骨带来的加成变化可以即时反映而又不产生无谓的重排。
 */
public final class PlayerCapabilityPanel extends UIElement {

    /** 面板相关语言键前缀。 */
    private static final String LANG = "gui.science_and_magic.player_capability_gui.";

    private static final float PANEL_WIDTH = 200.0F;
    private static final float PANEL_HEIGHT = 208.0F;
    private static final float PADDING = 6.0F;
    private static final float SECTION_GAP = 4.0F;
    private static final float COLUMN_GAP = 10.0F;
    private static final float COLUMN_HEIGHT = 118.0F;
    private static final float TITLE_HEIGHT = 12.0F;
    private static final float MODEL_HEIGHT = 56.0F;
    private static final float GROUP_TITLE_HEIGHT = 10.0F;
    private static final float ROW_HEIGHT = 10.0F;
    private static final float ABBR_WIDTH = 34.0F;
    private static final float FONT_SIZE = 8.0F;
    private static final float TITLE_FONT_SIZE = 9.0F;

    private static final int COLOR_HEADING = 0xFF60A5FA;
    private static final int COLOR_ABBR = 0xFFA0A0A0;
    private static final int COLOR_VALUE = 0xFFE6E6E6;

    /** 基础：bATK / bDEF。 */
    private static final List<AttributeEntry> BASE_ENTRIES = List.of(
            new AttributeEntry("bATK", CombatAttributes.BASE_ATTACK, "tooltip_base_attack", false),
            new AttributeEntry("bDEF", CombatAttributes.BASE_DEFEND, "tooltip_base_defend", false));

    /** 致命：BLD / BLDk。 */
    private static final List<AttributeEntry> BLOOD_ENTRIES = List.of(
            new AttributeEntry("BLD", CombatAttributes.BLOOD_COUNTER, "tooltip_blood_counter", true),
            new AttributeEntry("BLDk", CombatAttributes.BLOOD_COUNT, "tooltip_blood_count", false));

    /** 加成：pATK / mATK / sATK。 */
    private static final List<AttributeEntry> BONUS_ENTRIES = List.of(
            new AttributeEntry("pATK", CombatAttributes.PHYSIC_ATTACK, "tooltip_physic_attack", true),
            new AttributeEntry("mATK", CombatAttributes.MAGIC_ATTACK, "tooltip_magic_attack", true),
            new AttributeEntry("sATK", CombatAttributes.SCIENCE_ATTACK, "tooltip_science_attack", true));

    /** 防御与加权：pDEF / mDEF / PE / MC。 */
    private static final List<AttributeEntry> DEFENSE_ENTRIES = List.of(
            new AttributeEntry("pDEF", CombatAttributes.PHYSIC_DEFEND, "tooltip_physic_defend", true),
            new AttributeEntry("mDEF", CombatAttributes.MAGIC_DEFEND, "tooltip_magic_defend", true),
            new AttributeEntry("PE", CombatAttributes.PLAYER_ENERGY, "tooltip_player_energy", true),
            new AttributeEntry("MC", CombatAttributes.MAGIC_COUNT, "tooltip_magic_count", true));

    /** 特殊：mEP。 */
    private static final List<AttributeEntry> SPECIAL_ENTRIES = List.of(
            new AttributeEntry("mEP", CombatAttributes.MAX_EP, "tooltip_max_ep", false));

    private final List<ValueBinding> bindings = new ArrayList<>();

    public PlayerCapabilityPanel() {
        // panel_bg 来自 LDLib2 自带的 modern 主题（assets/ldlib2/lss/modern.lss）：
        // 深色圆角卡片背景 + 内边距；不套这个类的话根元素是透明的（看上去「没有背景」）。
        addClass("panel_bg");
        layout(layout -> layout.width(PANEL_WIDTH)
                .height(PANEL_HEIGHT)
                .paddingAll(PADDING)
                .gapAll(SECTION_GAP)
                .flexDirection(FlexDirection.COLUMN)
                .alignItems(AlignItems.STRETCH));
        addChild(buildHeading());
        addChild(new PlayerModelElement().layout(layout -> layout.widthStretch().height(MODEL_HEIGHT)));
        addChild(buildColumns());
        refresh();
    }

    /**
     * 每 tick 刷新一次数值，使装备/魂骨带来的属性变化即时可见。
     */
    @Override
    public void screenTick() {
        super.screenTick();
        refresh();
    }

    private void refresh() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        for (ValueBinding binding : this.bindings) {
            binding.refresh(player);
        }
    }

    private Label buildHeading() {
        Label heading = new Label();
        heading.setText(Component.translatable(LANG + "label_player_capability_gui"));
        heading.layout(layout -> layout.widthStretch().height(TITLE_HEIGHT));
        heading.textStyle(style -> style.textColor(COLOR_HEADING)
                .fontSize(TITLE_FONT_SIZE)
                .textAlignHorizontal(Horizontal.CENTER)
                .textShadow(false));
        return heading;
    }

    private UIElement buildColumns() {
        UIElement left = new UIElement();
        left.layout(layout -> layout.flexGrow(1.0F).flexDirection(FlexDirection.COLUMN).gapAll(SECTION_GAP));
        left.addChildren(buildGroup("group_base", BASE_ENTRIES),
                buildGroup("group_blood", BLOOD_ENTRIES),
                buildGroup("group_bonus", BONUS_ENTRIES));

        UIElement right = new UIElement();
        right.layout(layout -> layout.flexGrow(1.0F).flexDirection(FlexDirection.COLUMN).gapAll(SECTION_GAP));
        right.addChildren(buildGroup("group_defense", DEFENSE_ENTRIES),
                buildGroup("group_special", SPECIAL_ENTRIES));

        UIElement columns = new UIElement();
        columns.layout(layout -> layout.widthStretch()
                .height(COLUMN_HEIGHT)
                .gapAll(COLUMN_GAP)
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.FLEX_START));
        columns.addChildren(left, right);
        return columns;
    }

    private UIElement buildGroup(String titleKey, List<AttributeEntry> entries) {
        UIElement group = new UIElement();
        group.layout(layout -> layout.widthStretch().flexDirection(FlexDirection.COLUMN).gapAll(1.0F));

        Label title = new Label();
        title.setText(Component.translatable(LANG + titleKey));
        title.layout(layout -> layout.widthStretch().height(GROUP_TITLE_HEIGHT));
        title.textStyle(style -> style.textColor(COLOR_HEADING)
                .fontSize(FONT_SIZE)
                .textAlignHorizontal(Horizontal.LEFT)
                .textShadow(false));
        group.addChild(title);

        for (AttributeEntry entry : entries) {
            group.addChild(buildRow(entry));
        }
        return group;
    }

    private UIElement buildRow(AttributeEntry entry) {
        Component tooltip = Component.translatable(LANG + entry.tooltipKey());

        Label abbr = new Label();
        abbr.setText(Component.literal(entry.abbr()));
        abbr.layout(layout -> layout.width(ABBR_WIDTH).height(ROW_HEIGHT));
        abbr.textStyle(style -> style.textColor(COLOR_ABBR)
                .fontSize(FONT_SIZE)
                .textAlignHorizontal(Horizontal.LEFT));
        abbr.style(style -> style.tooltips(tooltip));

        Label value = new Label();
        value.setText(Component.literal("--"));
        value.layout(layout -> layout.flexGrow(1.0F).height(ROW_HEIGHT));
        value.textStyle(style -> style.textColor(COLOR_VALUE)
                .fontSize(FONT_SIZE)
                .textAlignHorizontal(Horizontal.RIGHT));
        value.style(style -> style.tooltips(tooltip));

        UIElement row = new UIElement();
        row.layout(layout -> layout.widthStretch()
                .height(ROW_HEIGHT)
                .flexDirection(FlexDirection.ROW)
                .alignItems(AlignItems.CENTER));
        row.style(style -> style.tooltips(tooltip));
        row.addChildren(abbr, value);

        this.bindings.add(new ValueBinding(entry, value));
        return row;
    }

    /**
     * 按「一位小数；百分比带 %」格式化属性值。
     */
    private static String format(AttributeEntry entry, double value) {
        return entry.percent()
                ? String.format(Locale.ROOT, "%.1f%%", value)
                : String.format(Locale.ROOT, "%.1f", value);
    }

    /** 一条属性的展示数据：缩写 + 属性 + 译名键 + 是否为百分比。 */
    private record AttributeEntry(String abbr, Holder<Attribute> attribute, String tooltipKey, boolean percent) {
    }

    /** 数值标签与属性的绑定，避免值未变化时的重复 setText。 */
    private static final class ValueBinding {
        private final AttributeEntry entry;
        private final Label label;
        private String last = "";

        private ValueBinding(AttributeEntry entry, Label label) {
            this.entry = entry;
            this.label = label;
        }

        private void refresh(Player player) {
            String text = format(this.entry, player.getAttributeValue(this.entry.attribute()));
            if (!text.equals(this.last)) {
                this.last = text;
                this.label.setText(Component.literal(text));
            }
        }
    }
}
