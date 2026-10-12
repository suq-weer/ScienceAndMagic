package team.xcmt.sam.api.wood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * 蓝图锚点检测：找出「蓝图的哪个格子该对齐到树苗矩阵的西北角」。
 * <p>
 * <b>为什么需要它</b>：原版模板地物会把结构按 {@code size / 2} 居中到目标点，而树蓝图是手工搭的，
 * 树干几乎不可能正好在包围盒正中。这里改为在蓝图里寻找<b>最低的那一层「存在完整 NxN 实心木质方块」</b>，
 * 把该 NxN 方格的西北角当作锚点。于是「蓝图局部原点在哪儿」「树干在盒子里靠哪边」都不影响对齐。
 * <p>
 * 这是一个纯工具类（除锚点缓存外无状态），被两处共用：
 * <ul>
 *     <li><b>datagen</b>：{@code TreeSourceLoader} 用 {@link TrunkBase#matched()} 判定蓝图是否合格，
 *         不合格直接让 {@code runData} 失败；</li>
 *     <li><b>运行时</b>：{@code WoodTemplateFeature} 放置时取 {@link TrunkBase#anchor()}。</li>
 * </ul>
 */
public final class TemplateAnchors {

    /**
     * 蓝图里用来标记「这是树的木质部分」的方块。
     * <p>
     * 列了 4 种原木变体是为了容忍「六面都是树皮的木」与去皮木——这些都是常见的树干写法。
     */
    public static final List<Block> TRUNK_MARKERS = List.of(
            Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_WOOD);

    /**
     * 已算出的锚点，按模板实例 + 矩阵边长缓存。
     * <p>
     * 外层用弱键：{@code /reload} 后 {@code StructureTemplateManager} 会清空并重建模板实例，
     * 旧键随之被回收，不会留下过期锚点。内层按 N 分开，因为同一张蓝图可能被不同大小的桶引用。
     */
    private static final Map<StructureTemplate, Map<Integer, TrunkBase>> CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private TemplateAnchors() {
    }

    /**
     * 锚点检测结果（模板相对坐标）。
     * <p>
     * 运行时只消费 {@link #anchor()}；其余字段用于把「为什么是这个锚点」说清楚——datagen 校验失败时
     * 直接把它们拼进异常消息，运行时则用于启动期/首次放置的诊断日志。
     *
     * @param anchor       树干基底的西北角（模板相对坐标）
     * @param layerY       命中的层 Y；回退时为最低标记层
     * @param support      命中方格在上一层（y+1）里的木质方块数，满值 {@code matrixSize²}
     * @param matrixSize   树苗矩阵边长 N
     * @param widestSquare 命中层的最大实心木质方格边长；回退时为所有层里最大的那个
     * @param layerWidest  自最低层起逐层的最大实心方格边长（失败诊断用）
     * @param fallback     是否为「没找到合格 NxN」的回退结果（此时 anchor 是旧的包围盒规则）
     */
    public record TrunkBase(BlockPos anchor, int layerY, int support, int matrixSize, int widestSquare,
            List<Integer> layerWidest, boolean fallback) {

        /** 是否找到了合格的 NxN 足迹。 */
        public boolean matched() {
            return !this.fallback;
        }

        /** 蓝图里一个木质标记方块都没有（此时 {@link #layerWidest()} 为空）。 */
        public boolean noMarkerAtAll() {
            return this.layerWidest.isEmpty();
        }
    }

    /** 取（并缓存）某张蓝图在指定矩阵边长下的树干锚点。 */
    public static TrunkBase trunkBase(StructureTemplate template, int matrixSize) {
        Map<Integer, TrunkBase> bySize = CACHE.computeIfAbsent(template, ignored -> new ConcurrentHashMap<>());
        return bySize.computeIfAbsent(matrixSize, size -> findTrunkBase(template, size));
    }

    /**
     * 树干锚点：<b>最低的那一层「存在完整 NxN 实心木质方块」</b>里最像树干的 NxN 方格的西北角。
     * <p>
     * 算法分四步：
     * <ol>
     *     <li>按 Y 分层收集木质标记点（{@link #TRUNK_MARKERS}）；</li>
     *     <li>自最低层起逐层跑「最大实心正方形」DP，找第一个 {@code 最大边长 >= N} 的层；</li>
     *     <li>在该层枚举所有「以 (x,z) 为西北角的 NxN 全填充」候选；</li>
     *     <li>用<b>上一层（y+1）的支撑度</b>消歧——树干是竖直连续的，上方木质方块最多的那格才是真树干；
     *         支撑度并列时取 x 最小、再取 z 最小，保证完全确定性。</li>
     * </ol>
     * 找不到合格层时回退到旧行为（最低标记层的包围盒西北角）并置 {@code fallback=true}：
     * datagen 会据此让 {@code runData} 失败；运行时则照常放置（树长歪总好过树不长）。
     */
    private static TrunkBase findTrunkBase(StructureTemplate template, int matrixSize) {
        StructurePlaceSettings settings = new StructurePlaceSettings();
        Map<Integer, List<BlockPos>> byLayer = new HashMap<>();

        for (Block marker : TRUNK_MARKERS) {
            // absolute = false：filterBlocks 返回「模板相对坐标」，与 placeInWorld 的变换同一坐标系。
            for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(BlockPos.ZERO, settings, marker, false)) {
                byLayer.computeIfAbsent(info.pos().getY(), ignored -> new ArrayList<>()).add(info.pos());
            }
        }

        if (byLayer.isEmpty()) {
            // 蓝图里没有任何木质标记方块：退回局部原点，并让调用方说明原因。
            return new TrunkBase(BlockPos.ZERO, 0, 0, matrixSize, 0, List.of(), true);
        }

        int minY = Collections.min(byLayer.keySet());
        int maxY = Collections.max(byLayer.keySet());

        // 回退锚点：与旧实现一致——最低标记层包围盒的西北角（minX / minZ 各自独立取）。
        int fallbackX = Integer.MAX_VALUE;
        int fallbackZ = Integer.MAX_VALUE;
        for (BlockPos pos : byLayer.get(minY)) {
            fallbackX = Math.min(fallbackX, pos.getX());
            fallbackZ = Math.min(fallbackZ, pos.getZ());
        }

        int sizeX = template.getSize().getX();
        int sizeZ = template.getSize().getZ();
        List<Integer> layerWidest = new ArrayList<>();
        int widestOverall = 0;

        for (int y = minY; y <= maxY; y++) {
            List<BlockPos> layer = byLayer.get(y);
            if (layer == null || layer.isEmpty()) {
                layerWidest.add(0);
                continue;
            }

            int[] dp = maximalSquareSides(layer, sizeX, sizeZ);
            int widest = 0;
            for (int value : dp) {
                widest = Math.max(widest, value);
            }
            layerWidest.add(widest);
            widestOverall = Math.max(widestOverall, widest);

            if (widest >= matrixSize) {
                return pickBestSquare(dp, sizeX, sizeZ, y, matrixSize, byLayer, maxY, widest, layerWidest);
            }
        }

        return new TrunkBase(new BlockPos(fallbackX, minY, fallbackZ), minY, 0, matrixSize, widestOverall,
                List.copyOf(layerWidest), true);
    }

    /**
     * 「最大实心正方形」DP：{@code dp[x * sizeZ + z]} = 以 {@code (x, z)} 为<b>东南角</b>的最大实心方格边长。
     * <p>
     * 于是「以 {@code (x0, z0)} 为西北角的 NxN 全填充」等价于 {@code dp[(x0+N-1) * sizeZ + (z0+N-1)] >= N}。
     */
    private static int[] maximalSquareSides(List<BlockPos> layer, int sizeX, int sizeZ) {
        boolean[] filled = new boolean[sizeX * sizeZ];
        for (BlockPos pos : layer) {
            int x = pos.getX();
            int z = pos.getZ();
            if (x >= 0 && x < sizeX && z >= 0 && z < sizeZ) {
                filled[x * sizeZ + z] = true;
            }
        }

        int[] dp = new int[sizeX * sizeZ];
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                int index = x * sizeZ + z;
                if (!filled[index]) {
                    continue;
                }
                int north = x > 0 ? dp[(x - 1) * sizeZ + z] : 0;
                int west = z > 0 ? dp[index - 1] : 0;
                int northWest = (x > 0 && z > 0) ? dp[(x - 1) * sizeZ + z - 1] : 0;
                dp[index] = Math.min(Math.min(north, west), northWest) + 1;
            }
        }
        return dp;
    }

    /**
     * 在合格层里挑最像树干的 NxN 方格。
     * <p>
     * 扫描顺序是 x 递增、z 递增，且只在支撑度<b>严格更大</b>时才替换，
     * 于是并列时自然保留「最小 x，再最小 z」——同一个蓝图永远得到同一个锚点。
     */
    private static TrunkBase pickBestSquare(int[] dp, int sizeX, int sizeZ, int y, int matrixSize,
            Map<Integer, List<BlockPos>> byLayer, int maxY, int widest, List<Integer> layerWidest) {
        Set<Long> above = y < maxY ? planarKeys(byLayer.get(y + 1)) : Set.of();

        int bestX = -1;
        int bestZ = -1;
        int bestSupport = -1;

        for (int x = 0; x + matrixSize <= sizeX; x++) {
            for (int z = 0; z + matrixSize <= sizeZ; z++) {
                if (dp[(x + matrixSize - 1) * sizeZ + (z + matrixSize - 1)] < matrixSize) {
                    continue;
                }
                int support = 0;
                for (int dx = 0; dx < matrixSize; dx++) {
                    for (int dz = 0; dz < matrixSize; dz++) {
                        if (above.contains(planarKey(x + dx, z + dz))) {
                            support++;
                        }
                    }
                }
                if (support > bestSupport) {
                    bestSupport = support;
                    bestX = x;
                    bestZ = z;
                }
            }
        }

        return new TrunkBase(new BlockPos(bestX, y, bestZ), y, Math.max(bestSupport, 0), matrixSize, widest,
                List.copyOf(layerWidest), false);
    }

    private static Set<Long> planarKeys(List<BlockPos> layer) {
        if (layer == null || layer.isEmpty()) {
            return Set.of();
        }
        Set<Long> keys = new HashSet<>(layer.size() * 2);
        for (BlockPos pos : layer) {
            keys.add(planarKey(pos.getX(), pos.getZ()));
        }
        return keys;
    }

    private static long planarKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
