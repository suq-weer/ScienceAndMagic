// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.LanguageProvider;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;

/**
 * 中文（zh_cn）语言文件。
 * <p>
 * 木种相关的文案全部按 {@link WoodMaterial#zhName()} 自动生成，新增木种无需手工增加键；
 * 其余模组文案在此集中维护。
 */
public final class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.science_and_magic.science_and_magic", "科技与魔法模组物品");

        // ---- 木种 ----
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            String id = material.name();
            String ns = material.namespace();
            String zh = material.zhName();
            String stripped = "去皮" + zh;

            add("block." + ns + "." + id + "_log", zh + "原木");
            add("block." + ns + "." + id + "_wood", zh);
            add("block." + ns + ".stripped_" + id + "_log", stripped + "原木");
            add("block." + ns + ".stripped_" + id + "_wood", stripped);
            add("block." + ns + "." + id + "_planks", zh + "木板");
            add("block." + ns + "." + id + "_stairs", zh + "楼梯");
            add("block." + ns + "." + id + "_slab", zh + "台阶");
            add("block." + ns + "." + id + "_fence", zh + "栅栏");
            add("block." + ns + "." + id + "_fence_gate", zh + "栅栏门");
            add("block." + ns + "." + id + "_door", zh + "门");
            add("block." + ns + "." + id + "_trapdoor", zh + "活板门");
            add("block." + ns + "." + id + "_button", zh + "按钮");
            add("block." + ns + "." + id + "_pressure_plate", zh + "压力板");
            add("block." + ns + "." + id + "_sign", zh + "告示牌");
            add("block." + ns + "." + id + "_wall_sign", zh + "墙上的告示牌");
            add("block." + ns + "." + id + "_hanging_sign", zh + "悬挂式告示牌");
            add("block." + ns + "." + id + "_wall_hanging_sign", zh + "墙上的悬挂式告示牌");
            add("block." + ns + "." + id + "_shelf", zh + "货架");
            add("block." + ns + "." + id + "_leaves", zh + "树叶");
            add("block." + ns + "." + id + "_sapling", zh + "树苗");

            add("item." + ns + "." + id + "_sign", zh + "告示牌");
            add("item." + ns + "." + id + "_hanging_sign", zh + "悬挂式告示牌");

            if (material.hasBoat()) {
                add("item." + ns + "." + id + "_boat", zh + "船");
                add("item." + ns + "." + id + "_chest_boat", zh + "运输船");
                add("entity." + ns + "." + id + "_boat", zh + "船");
                add("entity." + ns + "." + id + "_chest_boat", zh + "运输船");
            }
        }

        addCombatAttributeTranslations();
        addCombatItemTranslations();
        addCuriosTranslations();
        addElementTranslations();
        addGuiTranslations();
    }

    /** 魂骨与测试装备的显示名。 */
    private void addCombatItemTranslations() {
        add("item.science_and_magic.bone_head", "头部魂骨");
        add("item.science_and_magic.bone_body", "躯干魂骨");
        add("item.science_and_magic.bone_hand_left", "左臂魂骨");
        add("item.science_and_magic.bone_hand_right", "右臂魂骨");
        add("item.science_and_magic.bone_leg_left", "左腿魂骨");
        add("item.science_and_magic.bone_leg_right", "右腿魂骨");
        add("item.science_and_magic.test_sword", "测试用剑");
        add("item.science_and_magic.test_helmet", "测试用头盔");
        add("item.science_and_magic.test_chestplate", "测试用胸甲");
    }

    /** 12 项玩家战斗属性的显示名。 */
    private void addCombatAttributeTranslations() {
        add("attribute.name.science_and_magic.base_attack", "基础攻击力");
        add("attribute.name.science_and_magic.base_defend", "基础防御力");
        add("attribute.name.science_and_magic.blood_counter", "致命率");
        add("attribute.name.science_and_magic.blood_count", "致命加伤");
        add("attribute.name.science_and_magic.physic_attack", "物伤加成");
        add("attribute.name.science_and_magic.magic_attack", "法伤加成");
        add("attribute.name.science_and_magic.science_attack", "科技熟练加成");
        add("attribute.name.science_and_magic.physic_defend", "物伤防御率");
        add("attribute.name.science_and_magic.magic_defend", "法伤防御率");
        add("attribute.name.science_and_magic.max_ep", "魔力值上限");
        add("attribute.name.science_and_magic.player_energy", "体魄");
        add("attribute.name.science_and_magic.magic_count", "意志力");
    }

    private void addCuriosTranslations() {
        // 魂骨
        String id = "curios.identifier.";
        String mod = "curios.modifiers.";
        add(id+"bone_body", "§d[魂骨] §r躯干");
        add(id+"bone_hand_left", "§d[魂骨] §r左臂");
        add(id+"bone_hand_right", "§d[魂骨] §r右臂");
        add(id+"bone_head", "§d[魂骨] §r头骨");
        add(id+"bone_leg_left", "§d[魂骨] §r左腿");
        add(id+"bone_leg_right", "§d[魂骨] §r右腿");
        add(mod+"bone_body", "当穿在躯干时：");
        add(mod+"bone_hand_left", "当穿在左臂时：");
        add(mod+"bone_hand_right", "当穿在右臂时：");
        add(mod+"bone_head", "当穿在头骨时：");
        add(mod+"bone_leg_left", "当穿在左腿时：");
        add(mod+"bone_leg_right", "当穿在右腿时：");
    }

    private void addElementTranslations() {
        add("effect.science_and_magic.element_fire", "火元素附着");
        add("effect.science_and_magic.element_ice", "冰元素附着");
        add("effect.science_and_magic.element_water", "水元素附着");
        add("effect.science_and_magic.element_stone", "岩元素附着");
        add("effect.science_and_magic.element_wood", "木元素附着");
        add("effect.science_and_magic.element_lighting", "电元素附着");
        add("tooltip.science_and_magic.element_list", "拥有的元素附着: ");
        add("tooltip.science_and_magic.element_fire_short", "火");
        add("tooltip.science_and_magic.element_ice_short", "冰");
        add("tooltip.science_and_magic.element_water_short", "水");
        add("tooltip.science_and_magic.element_stone_short", "岩");
        add("tooltip.science_and_magic.element_wood_short", "木");
        add("tooltip.science_and_magic.element_lighting_short", "电");
        add("tooltip.science_and_magic.init_item", "蕴含着元素魔力的水晶球，或许可以把它的元素附着提取出来？");
        add("item.science_and_magic.fire_init", "火元素水晶球");
        add("item.science_and_magic.water_init", "水元素水晶球");
        add("item.science_and_magic.ice_init", "冰元素水晶球");
        add("item.science_and_magic.stone_init", "岩元素水晶球");
        add("item.science_and_magic.wood_init", "草元素水晶球");
        add("item.science_and_magic.lighting_init", "雷元素水晶球");
    }

    private void addGuiTranslations() {
        add("gui.science_and_magic.ep", "魔力值: ");
        add("gui.science_and_magic.player_capability_gui.tooltip_max_ep", "魔力值上限");
        add("gui.science_and_magic.player_capability_gui.tooltip_science_attack", "科技熟练加成");
        add("gui.science_and_magic.player_capability_gui.tooltip_blood_counter", "致命率");
        add("gui.science_and_magic.player_capability_gui.label_player_capability_gui", "玩家属性面板");
        add("gui.science_and_magic.player_capability_gui.tooltip_magic_defend", "法伤防御率");
        add("gui.science_and_magic.player_capability_gui.tooltip_magic_attack", "法伤加成");
        add("gui.science_and_magic.player_capability_gui.tooltip_physic_defend", "物伤防御率");
        add("gui.science_and_magic.player_capability_gui.tooltip_blood_count", "致命加伤");
        add("gui.science_and_magic.player_capability_gui.tooltip_base_attack", "基础攻击力");
        add("gui.science_and_magic.player_capability_gui.tooltip_player_energy", "体魄");
        add("gui.science_and_magic.player_capability_gui.tooltip_magic_count", "意志力");
        add("gui.science_and_magic.player_capability_gui.tooltip_base_defend", "基础防御力");
        add("gui.science_and_magic.player_capability_gui.tooltip_physic_attack", "物伤加成");
        add("gui.science_and_magic.player_capability_gui.group_base", "基础");
        add("gui.science_and_magic.player_capability_gui.group_blood", "致命");
        add("gui.science_and_magic.player_capability_gui.group_bonus", "加成");
        add("gui.science_and_magic.player_capability_gui.group_defense", "防御与加权");
        add("gui.science_and_magic.player_capability_gui.group_special", "特殊");
        add("gui.science_and_magic.player_capability_gui.button_open", "玩家属性");
        add("key.category.science_and_magic", "科技与魔法");
        add("key.science_and_magic.open_player_capability", "打开玩家属性面板");
    }
}
