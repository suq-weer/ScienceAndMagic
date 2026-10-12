package team.xcmt.sam.api.wood;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;

import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

import team.xcmt.sam.ScienceAndMagic;

/**
 * 内置的<b>代码几何形状库</b>：原版 {@code TreeFeatures} 里那 12 种树形的参数照抄。
 * <p>
 * 这些形状不进注册表（原因见 {@link TreePresetCatalog}），id 形如 {@code science_and_magic:pine}。
 * 预设的桶用 {@code .shape} 引用文件按<b>文件名</b>引用它们，例如
 * {@code src/main/resources/gen/tree_shapes/spruce/1x1/pine.shape} 就指向 {@link #PINE}。
 * 因此新增一个原版树形的预设**不需要**写 Java（丢文件 + 重跑 {@code runData} 即可），
 * 只有新增<b>几何算法</b>才需要在这里加一个常量。
 */
public final class TreeShapes {

    private static final Map<String, PlacerTreeShape> BY_PATH = new LinkedHashMap<>();

    /** 橡木：直干 4+2，球状树冠半径 2。 */
    public static final PlacerTreeShape OAK = define("oak", 1,
            (trunk, leaves, below) -> straightBlobTree(trunk, leaves, 4, 2, below).ignoreVines());

    /** 花式橡木：分叉粗干，树冠更高更密。 */
    public static final PlacerTreeShape FANCY_OAK = define("fancy_oak", 1,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new FancyTrunkPlacer(3, 11, 0),
                    leaves,
                    new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
                    new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)),
                    below).ignoreVines());

    /** 白桦：直干 5+2，球状树冠半径 2。 */
    public static final PlacerTreeShape BIRCH = define("birch", 1,
            (trunk, leaves, below) -> straightBlobTree(trunk, leaves, 5, 2, below).ignoreVines());

    /** 云杉：直干 5+2+1，锥形树冠。 */
    public static final PlacerTreeShape SPRUCE = define("spruce", 1,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new StraightTrunkPlacer(5, 2, 1),
                    leaves,
                    new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)),
                    new TwoLayersFeatureSize(2, 0, 2),
                    below).ignoreVines());

    /** 松树：直干 6+4，柱状树冠，比云杉更高更瘦。 */
    public static final PlacerTreeShape PINE = define("pine", 1,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new StraightTrunkPlacer(6, 4, 0),
                    leaves,
                    new PineFoliagePlacer(ConstantInt.of(1), ConstantInt.of(1), UniformInt.of(3, 4)),
                    new TwoLayersFeatureSize(2, 0, 2),
                    below).ignoreVines());

    /** 巨型云杉：2x2 巨干 13，长锥形树冠。 */
    public static final PlacerTreeShape MEGA_SPRUCE = define("mega_spruce", 2,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new GiantTrunkPlacer(13, 2, 14),
                    leaves,
                    new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(13, 17)),
                    new TwoLayersFeatureSize(1, 1, 2),
                    below));

    /** 巨型松：2x2 巨干 13，短锥形树冠。 */
    public static final PlacerTreeShape MEGA_PINE = define("mega_pine", 2,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new GiantTrunkPlacer(13, 2, 14),
                    leaves,
                    new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(3, 7)),
                    new TwoLayersFeatureSize(1, 1, 2),
                    below));

    /** 丛林树：直干 4+8。 */
    public static final PlacerTreeShape JUNGLE = define("jungle", 1,
            (trunk, leaves, below) -> straightBlobTree(trunk, leaves, 4, 8, below));

    /** 巨型丛林树：2x2 巨干 10+19。 */
    public static final PlacerTreeShape MEGA_JUNGLE = define("mega_jungle", 2,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new MegaJungleTrunkPlacer(10, 2, 19),
                    leaves,
                    new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
                    new TwoLayersFeatureSize(1, 1, 2),
                    below));

    /** 金合欢：分叉干，平顶树冠。 */
    public static final PlacerTreeShape ACACIA = define("acacia", 1,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new ForkingTrunkPlacer(5, 2, 2),
                    leaves,
                    new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)),
                    new TwoLayersFeatureSize(1, 0, 2),
                    below));

    /** 深色橡木：2x2 巨干 6（原版也只有 2x2 能生长）。 */
    public static final PlacerTreeShape DARK_OAK = define("dark_oak", 2,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new DarkOakTrunkPlacer(6, 2, 1),
                    leaves,
                    new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)),
                    new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty()),
                    below).ignoreVines());

    /** 樱花：弯曲干 + 垂枝树冠。 */
    public static final PlacerTreeShape CHERRY = define("cherry", 1,
            (trunk, leaves, below) -> new TreeConfiguration.TreeConfigurationBuilder(
                    trunk,
                    new CherryTrunkPlacer(7, 1, 0,
                            new WeightedListInt(WeightedList.<IntProvider>builder()
                                    .add(ConstantInt.of(1), 1)
                                    .add(ConstantInt.of(2), 1)
                                    .add(ConstantInt.of(3), 1)
                                    .build()),
                            UniformInt.of(2, 4),
                            UniformInt.of(-4, -3),
                            UniformInt.of(-1, 0)),
                    leaves,
                    new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5),
                            0.25F, 0.5F, 0.16666667F, 0.33333334F),
                    new TwoLayersFeatureSize(1, 0, 2),
                    below).ignoreVines());

    private TreeShapes() {
    }

    private static PlacerTreeShape define(String path, int size, PlacerTreeShape.PlacerFactory factory) {
        PlacerTreeShape shape = PlacerTreeShape.of(
                Identifier.fromNamespaceAndPath(ScienceAndMagic.MOD_ID, path), size, factory);
        BY_PATH.put(path, shape);
        return shape;
    }

    /**
     * 按 path 取内置形状，供 {@code .shape} 引用文件使用。
     *
     * @return 未定义时返回 {@code null}（扫描器会据此报错）
     */
    public static PlacerTreeShape byPath(String path) {
        return BY_PATH.get(path);
    }

    /** 全部内置形状的 path（用于错误提示）。 */
    public static java.util.Set<String> paths() {
        return java.util.Collections.unmodifiableSet(BY_PATH.keySet());
    }

    /**
     * 原版 {@code createStraightBlobTree}：直干 + 球状树冠。
     * <p>
     * 本模组用到的橡木 / 白桦 / 丛林三种形状的 {@code heightRandB} 恒为 0、树冠半径恒为 2，
     * 因此这两项直接内联。
     */
    private static TreeConfiguration.TreeConfigurationBuilder straightBlobTree(BlockStateProvider trunk,
            BlockStateProvider leaves, int baseHeight, int heightRandA, BlockStateProvider belowTrunk) {
        return new TreeConfiguration.TreeConfigurationBuilder(
                trunk,
                new StraightTrunkPlacer(baseHeight, heightRandA, 0),
                leaves,
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1),
                belowTrunk);
    }
}
