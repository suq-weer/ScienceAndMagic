package team.xcmt.sam.content;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import net.neoforged.neoforge.event.EventHooks;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.TreePreset;
import team.xcmt.sam.api.wood.TreePresetCatalog;
import team.xcmt.sam.api.wood.TreeShape;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.api.wood.WoodTreeFeatures;

/**
 * 木种树苗：把原版「只认 1x1 与 2x2」的生长逻辑换成<b>任意 NxN 矩阵</b>。
 * <p>
 * 原版 {@code TreeGrower} 内部写死了两种情形（{@code isTwoByTwoSapling} 只检查 4 个候选偏移），
 * 所以这里直接覆盖 {@link SaplingBlock#advanceTree}：从预设里最大的桶开始往下找，
 * 先找到「包含触发位置的合法 NxN 同种树苗方阵」就用哪个桶，桶内按权重抽一个形状。
 * <p>
 * 生长流程刻意照抄原版 {@code TreeGrower#growTree}：<b>先清空所有树苗、再放置，放置失败则原样恢复</b>。
 * 这样即使形状放置失败（例如空间不够），玩家摆好的树苗也不会平白消失。
 * <p>
 * 与预设的关联只存一个 {@code Identifier}（写进 codec），运行时经 {@link TreePresetCatalog} 反查
 * （目录由数据包重载监听器每次重载时装配），避免让方块持有注册表对象、也避免依赖客户端/服务端的加载顺序。
 */
public class WoodSaplingBlock extends SaplingBlock {

    public static final MapCodec<WoodSaplingBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TreeGrower.CODEC.fieldOf("tree").forGetter(block -> block.treeGrower),
            Identifier.CODEC.fieldOf("tree_preset").forGetter(block -> block.presetId),
            propertiesCodec())
            .apply(instance, WoodSaplingBlock::new));

    /** 与原版一致：只改方块状态、不触发邻居更新的写块标志（{@code UPDATE_INVISIBLE | UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS}）。 */
    private static final int QUIET_UPDATE = Block.UPDATE_INVISIBLE | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS;

    /**
     * 清空 / 恢复树苗矩阵用的写块标志：{@code UPDATE_NEIGHBORS | UPDATE_CLIENTS}。
     * <p>
     * 这里<b>不能</b>像原版那样用「不通知客户端」的 {@link #QUIET_UPDATE}：原版清掉的 4 格紧跟着
     * 就会被巨树干覆盖（覆盖时是带客户端通知的），所以不会有客户端残影；而我们的形状可以是蓝图，
     * 树干足迹未必铺满整块矩阵（甚至形状放置失败后要恢复），此时不同步就会让客户端一直看着
     * 已经不存在的树苗。代价只是每格一次邻居更新，矩阵最多 8x8，可以忽略。
     */
    private static final int SYNC_UPDATE = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;

    /** 一次生长尝试的结果。 */
    private enum Outcome {
        /** 已经长出树了。 */
        PLACED,
        /** 这次没长成（多半是空间不够），可以退一级去试更小的矩阵。 */
        RETRY_SMALLER,
        /** 不该用更小的矩阵顶替：事件被别的模组取消，或者地物根本不存在（配置错误）。 */
        ABORT
    }

    /** 已经报过「缺少地物」的键，避免每次生长尝试都刷日志。 */
    private static final Set<ResourceKey<ConfiguredFeature<?, ?>>> MISSING_FEATURES = ConcurrentHashMap.newKeySet();

    /** 已经报过「预设不存在」的预设 id。 */
    private static final Set<Identifier> MISSING_PRESETS = ConcurrentHashMap.newKeySet();

    private final Identifier presetId;

    public WoodSaplingBlock(TreeGrower treeGrower, Identifier presetId, BlockBehaviour.Properties properties) {
        super(treeGrower, properties);
        this.presetId = presetId;
    }

    @Override
    public MapCodec<? extends SaplingBlock> codec() {
        return CODEC;
    }

    /** 本树苗使用的树形预设 id。 */
    public Identifier presetId() {
        return this.presetId;
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        // 原版两段式：第一次只是把 STAGE 推到 1，第二次才真正长树。
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), QUIET_UPDATE);
            return;
        }

        WoodSet wood = WoodSet.bySapling(this);
        if (wood == null) {
            return;
        }
        TreePreset preset = TreePresetCatalog.get(this.presetId);
        if (preset == null) {
            reportMissingPreset();
            return;
        }

        for (int size = Math.min(preset.maxSize(), TreePreset.MAX_MATRIX_SIZE); size >= 1; size--) {
            WeightedList<TreeShape> bucket = preset.bucket(size);
            if (bucket == null || bucket.isEmpty()) {
                continue;
            }
            BlockPos anchor = findMatrix(level, pos, size, this);
            if (anchor == null) {
                continue;
            }
            TreeShape shape = bucket.getRandom(random).orElse(null);
            if (shape == null) {
                continue;
            }
            if (grow(level, anchor, size, state, shape, wood, random) != Outcome.RETRY_SMALLER) {
                // PLACED：长成了；ABORT：不要再拿更小的矩阵顶替（否则会把配置错误伪装成「树长得小」）
                return;
            }
        }
    }

    /**
     * 清空矩阵 → 放置 → 失败则恢复树苗。
     * <p>
     * 地物缺失（{@link Outcome#ABORT}）时必须<b>停止</b>而不是降级：形状来自预设 JSON、地物由
     * {@code runData} 烘焙，两者对不上通常意味着数据包删掉了地物或产物没重新生成。若此时降级，
     * 玩家摆一个 3x3 会莫名其妙长出一棵 1x1，把配置错误伪装成「树长得小」，极难排查
     * ——这正是 {@link #reportMissingFeature} 存在的理由。
     */
    private Outcome grow(ServerLevel level, BlockPos anchor, int size, BlockState sapling, TreeShape shape,
            WoodSet wood, RandomSource random) {
        ResourceKey<ConfiguredFeature<?, ?>> key = WoodTreeFeatures.configuredKey(wood.material(), shape);
        Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .get(key)
                .orElse(null);

        var event = EventHooks.fireBlockGrowFeature(level, random, anchor, holder);
        if (event.isCanceled()) {
            return Outcome.ABORT;
        }
        holder = event.getFeature();
        if (holder == null) {
            reportMissingFeature(wood, shape, key);
            return Outcome.ABORT;
        }

        // 用流体状态派生「空方块」：水下的树苗长完后原地应该还是水，而不是空气。
        BlockState empty = level.getFluidState(anchor).createLegacyBlock();
        fillMatrix(level, anchor, size, empty);

        if (holder.value().place(level, level.getChunkSource().getGenerator(), random, anchor)) {
            if (level.getBlockState(anchor) == empty) {
                level.sendBlockUpdated(anchor, sapling, empty, 2);
            }
            return Outcome.PLACED;
        }

        fillMatrix(level, anchor, size, sapling);
        return Outcome.RETRY_SMALLER;
    }

    private static void reportMissingFeature(WoodSet wood, TreeShape shape, ResourceKey<ConfiguredFeature<?, ?>> key) {
        if (MISSING_FEATURES.add(key)) {
            ScienceAndMagic.LOGGER.error(
                    "木种 {} 的形状 {} 缺少地物 {}：形状来自预设 JSON，而地物由 runData 烘焙，两者对不上。"
                            + "最常见的原因是改了 src/main/resources/gen/ 下的源却没重跑 runData，或数据包删掉了它。"
                            + "本次不生长，也不会退化成更小的树。",
                    wood.material().name(), shape.id(), key.identifier());
        }
    }

    /**
     * 预设 JSON 不存在：数据包可能删了它，或者目录压根没装配成功。
     * <p>
     * <b>把「当前实际装配了哪些预设」一并打出来</b>：这是区分「id 拼错 / 数据包覆盖」与
     * 「目录是空的」的唯一线索，否则两条路径的报错完全一样，排查只能靠猜。
     */
    private void reportMissingPreset() {
        if (MISSING_PRESETS.add(this.presetId)) {
            Collection<TreePreset> installed = TreePresetCatalog.all();
            if (installed.isEmpty()) {
                ScienceAndMagic.LOGGER.error("树苗的树形预设 {} 不存在，而且当前目录里一个预设都没有"
                        + "（读取 data/<命名空间>/tree_preset/*.json 得到 0 个）。这棵树苗不会生长。"
                        + "请确认 runData 跑过、并且 mod 的 data 已经随构建复制进运行目录。", this.presetId);
                return;
            }
            ScienceAndMagic.LOGGER.error("树苗的树形预设 {} 不存在。当前已装配的预设是 [{}]。"
                            + "请核对拼写，或确认数据包没有覆盖 / 删除它；这棵树苗不会生长。",
                    this.presetId,
                    installed.stream().map(preset -> preset.id().toString())
                            .collect(Collectors.joining(", ")));
        }
    }

    private static void fillMatrix(ServerLevel level, BlockPos anchor, int size, BlockState state) {
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                level.setBlock(anchor.offset(dx, 0, dz), state, SYNC_UPDATE);
            }
        }
    }

    /**
     * 找「包含触发位置、边长 size、全为同种树苗」的方阵最小角。
     * <p>
     * 扫描顺序让「触发位置正好是最小角」优先命中（与原版 2x2 的语义一致），
     * 因此玩家骨粉催熟方阵的西北角树苗时行为最直观；其余情况退化为确定性的第一个合法方阵。
     */
    private static BlockPos findMatrix(ServerLevel level, BlockPos pos, int size, Block sapling) {
        for (int dx = 0; dx > -size; dx--) {
            for (int dz = 0; dz > -size; dz--) {
                BlockPos candidate = pos.offset(dx, 0, dz);
                if (isFullMatrix(level, candidate, size, sapling)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean isFullMatrix(ServerLevel level, BlockPos min, int size, Block sapling) {
        if (!level.isLoaded(min) || !level.isLoaded(min.offset(size - 1, 0, size - 1))) {
            return false;
        }
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                if (!level.getBlockState(min.offset(dx, 0, dz)).is(sapling)) {
                    return false;
                }
            }
        }
        return true;
    }
}
