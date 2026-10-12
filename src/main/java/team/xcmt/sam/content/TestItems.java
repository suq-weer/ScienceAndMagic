// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.content;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

import net.neoforged.neoforge.registries.DeferredItem;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.combat.ElementType;

/**
 * 测试用武器与护甲：用来验证「属性词条按持有 / 穿戴槽位生效」。
 * <p>
 * 数值全部走原版 {@link DataComponents#ATTRIBUTE_MODIFIERS} 组件，因此 Tooltip 会自动显示
 * 「当拿在主手时：+3 基础攻击力」这类分组词条，无需手写提示文本。
 * <p>
 * 注意 26.2 已没有 {@code ArmorItem}，护甲的可穿戴能力由 {@link Equippable} 组件提供。
 */
public final class TestItems {

    /** 测试武器：主手 +3 bATK、+20% pATK，并附带二级火元素。 */
    public static final DeferredItem<Item> TEST_SWORD =
            ModBlocks.ITEMS.registerItem("test_sword", Item::new, properties -> properties.stacksTo(1)
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(CombatAttributes.BASE_ATTACK,
                                    modifier("test_sword_base_attack", 3.0),
                                    EquipmentSlotGroup.MAINHAND)
                            .add(CombatAttributes.PHYSIC_ATTACK,
                                    modifier("test_sword_physic_attack", 20.0),
                                    EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(ElementAttachmentSpec.COMPONENT,
                            ElementAttachmentSpec.weapon(ElementType.FIRE)));

    /** 测试头盔：头部 +2 bDEF、+10% PE。 */
    public static final DeferredItem<Item> TEST_HELMET =
            ModBlocks.ITEMS.registerItem("test_helmet", Item::new, properties -> properties.stacksTo(1)
                    .component(DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.HEAD).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                            .add(CombatAttributes.BASE_DEFEND,
                                    modifier("test_helmet_base_defend", 2.0),
                                    EquipmentSlotGroup.HEAD)
                            .add(CombatAttributes.PLAYER_ENERGY,
                                    modifier("test_helmet_player_energy", 10.0),
                                    EquipmentSlotGroup.HEAD)
                            .build()));

    /** 测试胸甲：胸部 +3 bDEF、+10% pDEF。 */
    public static final DeferredItem<Item> TEST_CHESTPLATE =
            ModBlocks.ITEMS.registerItem("test_chestplate", Item::new,
                    properties -> properties.stacksTo(1)
                            .component(DataComponents.EQUIPPABLE,
                                    Equippable.builder(EquipmentSlot.CHEST).build())
                            .component(DataComponents.ATTRIBUTE_MODIFIERS,
                                    ItemAttributeModifiers.builder()
                                            .add(CombatAttributes.BASE_DEFEND,
                                                    modifier("test_chestplate_base_defend", 3.0),
                                                    EquipmentSlotGroup.CHEST)
                                            .add(CombatAttributes.PHYSIC_DEFEND,
                                                    modifier("test_chestplate_physic_defend", 10.0),
                                                    EquipmentSlotGroup.CHEST)
                                            .build()));

    private TestItems() {
    }

    /** 触发类加载（条目在静态初始化时创建）。 */
    public static void init() {
        // 无需额外逻辑：测试物品是静态字段，调用本方法本身就会触发类初始化并完成注册。
    }

    /** 全部测试物品（创造栏用）。 */
    public static List<DeferredItem<Item>> all() {
        return List.of(TEST_SWORD, TEST_HELMET, TEST_CHESTPLATE);
    }

    private static AttributeModifier modifier(String name, double amount) {
        return new AttributeModifier(Identifier.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, name),
                amount, AttributeModifier.Operation.ADD_VALUE);
    }
}
