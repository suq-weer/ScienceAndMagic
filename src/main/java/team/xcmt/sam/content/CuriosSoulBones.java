// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.content;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.DeferredItem;

import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosDataComponents;

import team.xcmt.sam.ScienceAndMagic;

/**
 * 基于 Curios 的 6 部位魂骨。
 * <p>
 * 槽位由数据包定义（{@code data/science_and_magic/curios/slots/bone_*.json}），物品通过
 * 物品标签 {@code #curios:bone_*} 归属到对应槽位；属性词条用 Curios 的
 * {@link CuriosDataComponents#ATTRIBUTE_MODIFIERS} 组件表达，佩戴即生效并自动进入 Tooltip
 * （形如「当佩戴在 头骨 时：+5 意志力」）。
 * <p>
 * 属性取向（数值可调，见《战斗机制.md》）：
 * <ul>
 *     <li>头部 → 意志力 / 魔力上限</li>
 *     <li>躯干 → 体魄 / 基础防御力</li>
 *     <li>左右臂 → 基础攻击力 / 物伤加成</li>
 *     <li>左右腿 → 物伤防御率 / 基础防御力</li>
 * </ul>
 */
public final class CuriosSoulBones {

    /** 6 个魂骨部位，{@code id} 同时是物品名、槽位名与物品标签名。 */
    public enum Part {
        HEAD("bone_head"),
        BODY("bone_body"),
        HAND_LEFT("bone_hand_left"),
        HAND_RIGHT("bone_hand_right"),
        LEG_LEFT("bone_leg_left"),
        LEG_RIGHT("bone_leg_right");

        private final String id;

        Part(String id) {
            this.id = id;
        }

        public String id() {
            return this.id;
        }
    }

    /** 一条属性修饰：{@code attribute} 上加 {@code amount}。 */
    private record Bonus(Holder<Attribute> attribute, double amount) {
    }

    private static final Map<Part, DeferredItem<Item>> ITEMS = new EnumMap<>(Part.class);

    static {
        register(Part.HEAD,
                new Bonus(CombatAttributes.MAGIC_COUNT, 5.0),
                new Bonus(CombatAttributes.MAX_EP, 10.0));
        register(Part.BODY,
                new Bonus(CombatAttributes.PLAYER_ENERGY, 10.0),
                new Bonus(CombatAttributes.BASE_DEFEND, 2.0));
        register(Part.HAND_LEFT,
                new Bonus(CombatAttributes.BASE_ATTACK, 1.0),
                new Bonus(CombatAttributes.PHYSIC_ATTACK, 5.0));
        register(Part.HAND_RIGHT,
                new Bonus(CombatAttributes.BASE_ATTACK, 1.0),
                new Bonus(CombatAttributes.PHYSIC_ATTACK, 5.0));
        register(Part.LEG_LEFT,
                new Bonus(CombatAttributes.PHYSIC_DEFEND, 5.0),
                new Bonus(CombatAttributes.BASE_DEFEND, 1.0));
        register(Part.LEG_RIGHT,
                new Bonus(CombatAttributes.PHYSIC_DEFEND, 5.0),
                new Bonus(CombatAttributes.BASE_DEFEND, 1.0));
    }

    private CuriosSoulBones() {
    }

    /** 触发类加载（条目在静态初始化时创建）。 */
    public static void init() {
        // 无需额外逻辑：调用本方法本身就会触发类初始化，静态块中的 6 个魂骨随即注册。
    }

    private static void register(Part part, Bonus... bonuses) {
        DeferredItem<Item> item = ModBlocks.ITEMS.registerItem(part.id(), Item::new, properties -> {
            CurioAttributeModifiers.Builder builder = CurioAttributeModifiers.builder();
            for (int index = 0; index < bonuses.length; index++) {
                Bonus bonus = bonuses[index];
                builder.addModifier(bonus.attribute(),
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(ScienceAndMagic.MOD_ID,
                                        part.id() + "_" + index),
                                bonus.amount(), AttributeModifier.Operation.ADD_VALUE),
                        part.id());
            }
            return properties.stacksTo(1)
                    .component(CuriosDataComponents.ATTRIBUTE_MODIFIERS,
                            builder.build().withTooltip(true));
        });
        ITEMS.put(part, item);
    }

    /** 某个部位对应的物品。 */
    public static DeferredItem<Item> item(Part part) {
        return ITEMS.get(part);
    }

    /** 全部魂骨物品（创造栏用）。 */
    public static List<DeferredItem<Item>> all() {
        return new ArrayList<>(ITEMS.values());
    }
}
