package team.xcmt.sam.api.wood;

import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;

/**
 * NBT 蓝图形状：形状来自手工搭建、用结构方块导出的 {@code .nbt}。
 * <p>
 * <b>「文件路径」与「资源 id」是两回事，本类负责由同一条「形状路径」推出两者，因此不可能走样。</b>
 * 设形状路径为 {@code fancy/3x3/fancy_big_tree}：
 * <ul>
 *     <li><b>文件</b>：{@code data/<namespace>/structure/tree_shapes/fancy/3x3/fancy_big_tree.nbt}
 *         —— 必须在 {@code structure/} 之下，原版 {@code StructureTemplateManager} 只从那里读；</li>
 *     <li><b>模板 id</b>：{@code <namespace>:tree_shapes/fancy/3x3/fancy_big_tree}
 *         —— <b>没有</b> {@code structure/} 前缀。原版读盘时用
 *         {@code FileToIdConverter.json("structure")}，它会把这个前缀剥掉，正如
 *         {@code data/<ns>/recipe/x.json} 的 id 是 {@code <ns>:x} 而不是 {@code <ns>:recipe/x}。</li>
 * </ul>
 * 这里曾经手写模板 id 并把 {@code structure/} 混了进去，结果运行期永远查不到蓝图：
 * 放置失败 → 生长逻辑按「本次没长成」降级到更小的矩阵。所以现在只允许经
 * {@link #of(String, String, int)} / {@link #fromTemplate(Identifier, int, List)} 构造。
 * <p>
 * <b>锚点由代码自动做「树干足迹匹配」，你不需要操心</b>：放置时会在蓝图里找<b>最低的那一层
 * 「存在完整 NxN 实心木质方块」</b>（木质 = {@code oak_log} / {@code oak_wood} /
 * {@code stripped_oak_log} / {@code stripped_oak_wood}），把该 NxN 方格的西北角对齐到树苗矩阵的
 * 西北角。N 就是 {@link #size()}，也就是桶目录名 {@code <N>x<N>}。算法细节见 {@link TemplateAnchors}。
 * <p>
 * 若整张蓝图都找不到完整的 NxN 实心木质层（例如 3x3 桶里放了 2x2 树干的蓝图），
 * <b>重跑 {@code runData} 时会直接失败</b>并给出逐层诊断，因此这类错误进不了游戏。
 * <p>
 * 蓝图里的方块由 {@code science_and_magic:wood_swap} 处理器按木种替换：
 * 橡木系（{@code oak_*} 全部衍生方块）换成该木种的对应方块，空气被当作结构空位跳过（不覆盖原地形），
 * 其余方块原样保留。
 *
 * @param id        形状 id，例如 {@code science_and_magic:fancy/3x3/fancy_big_tree}
 * @param size      需要的树苗矩阵边长 N
 * @param template  蓝图模板 id（{@code StructureTemplateManager} 使用的那个），
 *                  例如 {@code science_and_magic:tree_shapes/fancy/3x3/fancy_big_tree}
 * @param rotations 允许的随机旋转（默认只有 {@link Rotation#NONE}，避免手工蓝图被转得看不出原样）
 */
public record TemplateTreeShape(Identifier id, int size, Identifier template, List<Rotation> rotations)
        implements TreeShape {

    /** 蓝图在 {@code data/<namespace>/structure/} 之下的子目录名。 */
    public static final String TEMPLATE_DIR = "tree_shapes";

    public TemplateTreeShape {
        rotations = List.copyOf(rotations);
    }

    /**
     * 由「形状路径」构造。两个 id 都从这一个入参推出。
     *
     * @param namespace 木种所属命名空间
     * @param shapePath 形状路径，即 {@code <预设>/<N>x<N>/<名称>}，例如 {@code fancy/3x3/fancy_big_tree}
     * @param size      树苗矩阵边长 N，必须与桶目录一致
     */
    public static TemplateTreeShape of(String namespace, String shapePath, int size) {
        return new TemplateTreeShape(
                Identifier.fromNamespaceAndPath(namespace, shapePath),
                size,
                Identifier.fromNamespaceAndPath(namespace, TEMPLATE_DIR + "/" + shapePath),
                List.of(Rotation.NONE));
    }

    /**
     * 由模板 id 反推（运行时读预设 JSON 时用）。
     * <p>
     * 一律走这里而不是手写字符串裁剪：前缀不符时<b>直接抛错</b>，而不是悄悄退回「拿整条路径当形状路径」
     * ——那会让地物 id 全部对不上，症状是「树苗不长」这种极难排查的形态。
     */
    public static TemplateTreeShape fromTemplate(Identifier template, int size, List<Rotation> rotations) {
        String prefix = TEMPLATE_DIR + "/";
        String path = template.getPath();
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException("蓝图模板 id " + template + " 必须以 " + prefix + " 开头"
                    + "（即蓝图要放在 data/<命名空间>/structure/" + TEMPLATE_DIR + "/ 之下）");
        }
        return new TemplateTreeShape(
                Identifier.fromNamespaceAndPath(template.getNamespace(), path.substring(prefix.length())),
                size, template, rotations);
    }

    /**
     * 蓝图在 {@code data/<namespace>/} 之下的相对路径，例如
     * {@code structure/tree_shapes/fancy/3x3/fancy_big_tree.nbt}。
     * <p>
     * 由 {@link #template} 推出，所以「写出去的文件」与「运行时查的 id」永远指向同一个东西。
     */
    public String templateFile() {
        return "structure/" + this.template.getPath() + ".nbt";
    }

    /** 人可读的「期望文件位置」，用于运行期诊断（相对存档的数据包根）。 */
    public static String expectedFile(Identifier template) {
        return "data/" + template.getNamespace() + "/structure/" + template.getPath() + ".nbt";
    }
}
