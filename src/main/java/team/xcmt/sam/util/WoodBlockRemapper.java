// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import team.xcmt.sam.api.wood.WoodSet;

/**
 * 蓝图方块替换器：把蓝图里的<b>原版橡木系</b>方块换成目标木种的对应方块。
 * <p>
 * 覆盖橡木系<b>全部衍生方块</b>，不只是原木与树叶：
 * {@code log / wood / stripped_log / stripped_wood / planks / stairs / slab / fence / fence_gate /
 * door / trapdoor / button / pressure_plate / sign / wall_sign / hanging_sign / wall_hanging_sign /
 * shelf / leaves / sapling}。
 * <p>
 * 两条安全规则：
 * <ul>
 *     <li>只保留<b>目标方块也有</b>的同名属性，其余用默认值——否则给台阶/楼梯/门这类方块塞一个不存在的
 *         属性会直接抛异常；</li>
 *     <li>目标木种没有对应方块时返回 {@code null}，调用方应保留原方块，而不是静默变成空气。</li>
 * </ul>
 * 空气不在本类的职责范围内：跳过空气由 {@code WoodSwapProcessor} 处理（等价于结构空位）。
 */
public final class WoodBlockRemapper {

    /** 蓝图里约定用来标记「这是木质部分」的原版木种前缀。 */
    public static final String MARKER_PREFIX = "oak";

    private static final String STRIPPED_PREFIX = "stripped_" + MARKER_PREFIX + "_";

    private WoodBlockRemapper() {
    }

    /**
     * 把一个方块状态映射到目标木种。
     *
     * @return 映射后的状态；原方块不属于橡木系、或目标木种没有对应方块时返回 {@code null}
     */
    public static BlockState remap(BlockState source, WoodSet wood) {
        String suffix = suffixOf(BuiltInRegistries.BLOCK.getKey(source.getBlock()));
        if (suffix == null) {
            return null;
        }
        Block target = wood.block(suffix);
        if (target == null) {
            return null;
        }
        return copyProperties(source, target);
    }

    /**
     * 从原版方块 id 里取出「木种之后的后缀」。
     * <p>
     * {@code minecraft:oak_planks → planks}、{@code minecraft:stripped_oak_wood → stripped_wood}；
     * 非 {@code minecraft} 命名空间或不是橡木系时返回 {@code null}。
     */
    public static String suffixOf(Identifier blockId) {
        if (!"minecraft".equals(blockId.getNamespace())) {
            return null;
        }
        String path = blockId.getPath();
        if (path.startsWith(STRIPPED_PREFIX)) {
            return "stripped_" + path.substring(STRIPPED_PREFIX.length());
        }
        if (path.startsWith(MARKER_PREFIX + "_")) {
            return path.substring(MARKER_PREFIX.length() + 1);
        }
        return null;
    }

    /**
     * 把 {@code source} 的属性搬到 {@code target} 上：只搬两边同名且取值类型一致的属性。
     */
    public static BlockState copyProperties(BlockState source, Block target) {
        BlockState result = target.defaultBlockState();
        for (Property<?> property : source.getProperties()) {
            Property<?> own = target.getStateDefinition().getProperty(property.getName());
            if (own != null && own.getValueClass().equals(property.getValueClass())) {
                result = copy(result, own, property, source);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copy(BlockState target, Property<T> targetProperty,
            Property<?> sourceProperty, BlockState source) {
        return target.setValue(targetProperty, (T) source.getValue(sourceProperty));
    }
}
