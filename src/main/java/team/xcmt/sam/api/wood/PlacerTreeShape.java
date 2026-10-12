// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;

/**
 * 代码几何形状：树干 + 树冠由原版的 {@code TrunkPlacer} / {@code FoliagePlacer} 组合而成。
 * <p>
 * 几何数值来自原版 {@code net.minecraft.data.worldgen.features.TreeFeatures}，具体写在
 * {@link TreeShapes} 里。因为形状与木种解耦，这里只描述「长什么样」，
 * 「用谁的原木和树叶」由 {@link #createConfiguration(WoodSet)} 在数据生成时注入。
 *
 * @param factory 三个方块提供者（树干 / 树叶 / 树干下方的土）→ 树配置构造器
 */
public record PlacerTreeShape(Identifier id, int size, PlacerFactory factory) implements TreeShape {

    @FunctionalInterface
    public interface PlacerFactory {
        TreeConfiguration.TreeConfigurationBuilder create(BlockStateProvider trunk, BlockStateProvider leaves,
                BlockStateProvider belowTrunk);
    }

    public static PlacerTreeShape of(Identifier id, int size, PlacerFactory factory) {
        return new PlacerTreeShape(id, size, factory);
    }

    /**
     * 用指定木种的方块构造 {@link TreeConfiguration}。
     * <p>
     * 树干下方始终铺一层土（原版 {@code CAN_PLACE_BELOW_TREE_TRUNKS}），
     * 让长在沙地 / 泥土上的树不会露出悬空的树干底部。
     */
    public TreeConfiguration createConfiguration(WoodSet set) {
        return this.factory.create(
                BlockStateProvider.simple(set.log().get()),
                BlockStateProvider.simple(set.leaves().get()),
                RuleBasedStateProvider.ifTrueThenProvide(TreeConfiguration.CAN_PLACE_BELOW_TREE_TRUNKS, Blocks.DIRT))
                .build();
    }
}
