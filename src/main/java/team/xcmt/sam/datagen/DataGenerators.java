package team.xcmt.sam.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;

import net.neoforged.neoforge.data.event.GatherDataEvent;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;

/**
 * 数据生成总入口，由 {@link net.neoforged.neoforge.data.event.GatherDataEvent} 触发。
 * <p>
 * 执行 {@code gradlew runData} 后产物写入 {@code src/generated/resources}。
 */
public final class DataGenerators {

    private DataGenerators() {
    }

    /** {@link GatherDataEvent} 是抽象类，26.2 要求分别监听它的两个子类。 */
    public static void gatherDataClient(GatherDataEvent.Client event) {
        gatherData(event);

        // ---- 仅客户端可用的提供器 ----
        // 图集定义依赖客户端的 sprite source 类型（SpriteSource/SingleFile），
        // 因此只在 clientData 运行中注册，避免服务端数据生成时加载客户端类。
        DataGenerator generator = event.getGenerator();
        generator.addProvider(true, new WoodAtlasProvider(generator.getPackOutput(),
                event.getLookupProvider(), ScienceAndMagic.MOD_ID));
    }

    /** {@link GatherDataEvent} 是抽象类，26.2 要求分别监听它的两个子类。 */
    public static void gatherDataServer(GatherDataEvent.Server event) {
        gatherData(event);
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        String modId = ScienceAndMagic.MOD_ID;

        // ---- 客户端资源（blockstates / 模型 / 物品定义 / 烘焙贴图 / 语言）----
        generator.addProvider(true, new WoodBlockStateProvider(output));
        generator.addProvider(true, new WoodModelProvider(output));
        generator.addProvider(true, new WoodItemDefinitionProvider(output));
        // 实体贴图与告示牌编辑界面贴图无法进入图集，需要在这里读原版贴图并用同样的 OKLab 参数烘焙。
        generator.addProvider(true,
                new ModBakedTextureProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        generator.addProvider(true, new ModLanguageProvider(output, modId, "zh_cn"));

        // ---- 服务端数据（标签 / 配方 / 战利品 / 数据映射 / 世界生成）----
        generator.addProvider(true, new ModBlockTagsProvider(output, lookup, modId));
        generator.addProvider(true, new ModItemTagsProvider(output, lookup, modId));
        generator.addProvider(true, new WoodRecipeProvider(output));
        generator.addProvider(true, new WoodLootProvider(output));
        generator.addProvider(true, new ModDataMapProvider(output, lookup));
        // 树形预设与蓝图副本：读 src/main/resources/gen/tree_shapes/（手写源）→ 全量校验 →
        // 写 tree_preset/*.json 与蓝图字节副本。
        // 它和 ModWorldGenProvider 共用同一份惰性装载结果，先跑哪个都一样。
        generator.addProvider(true, new WoodTreePresetProvider(output));
        generator.addProvider(true, new ModWorldGenProvider(output, lookup));

        // 地图色不写进任何产物（它在代码里生效），所以在数据生成时也打一次，方便与游戏启动日志对照。
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            ScienceAndMagic.LOGGER.info("  - {}: 地图色 {}", material.name(),
                    material.palette().describeMapColors());
        }

        ScienceAndMagic.LOGGER.info("Science And Magic: data generators registered");
    }
}
