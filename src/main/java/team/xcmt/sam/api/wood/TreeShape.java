package team.xcmt.sam.api.wood;

import net.minecraft.resources.Identifier;

/**
 * 树形<b>形状</b>：一种具体的树干 + 树冠几何。
 * <p>
 * 两种实现：
 * <ul>
 *     <li>{@link PlacerTreeShape} —— <b>代码几何</b>：由原版的 TrunkPlacer / FoliagePlacer 组合而成
 *         （参数照抄原版 {@code TreeFeatures}）</li>
 *     <li>{@link TemplateTreeShape} —— <b>NBT 蓝图</b>：结构方块导出的 {@code .nbt}</li>
 * </ul>
 * 形状本身与木种无关：木种在数据生成阶段把自己的原木 / 树叶注入，因此同一形状可被任意木种复用。
 * <p>
 * 形状<b>不进注册表</b>（原因见 {@link TreePresetCatalog}）：代码几何由 {@link TreeShapes} 按 path 持有，
 * 蓝图形状由 {@code src/main/resources/gen/tree_shapes/**} 目录在 {@code runData} 时建立。
 * <p>
 * <b>size 的语义</b>：生长这棵树需要多大的树苗矩阵（{@code N x N}）。代码几何里原版只有 1x1 与 2x2，
 * 而蓝图可以是 3x3、4x4…… 预设据此分桶。
 */
public interface TreeShape {

    /** 形状 id，例如 {@code science_and_magic:pine} 或 {@code science_and_magic:fancy/3x3/fancy_big_tree}。 */
    Identifier id();

    /** 生长所需的树苗矩阵边长 N（1x1 为 1，2x2 为 2，依此类推）。 */
    int size();

    /** 形状 id 的路径部分。 */
    default String path() {
        return this.id().getPath();
    }

    /** 扁平化路径：把 {@code /} 换成 {@code _}，用于拼地物 id（Identifier 路径不允许斜杠以外的特殊字符）。 */
    default String flatPath() {
        return this.path().replace('/', '_');
    }

    /** 路径的最后一个片段，例如 {@code pine} 或 {@code tower}。 */
    default String name() {
        String path = this.path();
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    /**
     * 地物 id 的后缀，例如 {@code 1x1_pine} / {@code 3x3_tower}。
     * <p>
     * 带上 N 是为了「同一木种同时拥有 {@code 1x1/tower.nbt} 与 {@code 3x3/tower.nbt}」时不会撞名。
     */
    default String featureSuffix() {
        return this.size() + "x" + this.size() + "_" + this.name();
    }
}
