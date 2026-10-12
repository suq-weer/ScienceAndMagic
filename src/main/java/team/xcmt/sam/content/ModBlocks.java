// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.content;

import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.registries.DeferredRegister;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodPalette;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.api.wood.WoodTexture;

import java.util.List;

/**
 * 所有方块 / 物品 / 实体的注册表，以及全部木种实例。
 * <p>
 * <b>新增木种</b>：在这里加一个 {@link WoodMaterial} 常量 + 一行 {@link WoodSet#register}，
 * 然后运行 {@code gradlew runData} 即可生成对应的模型 / blockstate / 物品定义 / 标签 / 配方 /
 * 战利品 / 语言 / 世界生成；贴图则由 {@link WoodPalette} 的目标色自动派生。
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ScienceAndMagic.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ScienceAndMagic.MOD_ID);
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ScienceAndMagic.MOD_ID);

    private static WoodSet register(WoodMaterial woodMaterial) {
        return WoodSet.register(BLOCKS, ITEMS, ENTITIES, woodMaterial);
    }

    // 红衫
    public static final int SEQUOIA_COLOR = 0xffa97848;
    public static final int SEQUOIA_LEAVE_COLOR = 0xff48a960;
    public static final WoodSet SEQUOIA_SET = register(WoodMaterial.of(
            ScienceAndMagic.MOD_ID,
            "sequoia",
            "红杉木",
            WoodPalette.builder(SEQUOIA_COLOR).leaves(SEQUOIA_LEAVE_COLOR).color(WoodTexture.SAPLING, SEQUOIA_COLOR).build(),
            ScienceAndMagic.id("conifer"),
            true,
            true
    ));

    // 黑泌树
    public static final int BLACK_SEEP_TREE_COLOR = 0xff2e2434;
    public static final int BLACK_SEEP_LEAVE_COLOR = 0xff24342a;
    public static final WoodSet BLOCK_SEEP_TREE = register(WoodMaterial.of(
            ScienceAndMagic.MOD_ID,
            "black_seep_tree",
            "黑泌树",
            WoodPalette.builder(BLACK_SEEP_TREE_COLOR).leaves(BLACK_SEEP_LEAVE_COLOR).color(WoodTexture.SAPLING, BLACK_SEEP_TREE_COLOR).build(),
            ScienceAndMagic.id("fancy"),
            List.of(BiomeTags.IS_FOREST),
            true,
            true
    ));

}
