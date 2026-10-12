package team.xcmt.sam.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.TreePreset;
import team.xcmt.sam.api.wood.TreePresetCatalog;

/**
 * 运行时把 {@code data/<namespace>/tree_preset/*.json} 读成 {@link TreePresetCatalog}。
 * <p>
 * 挂在 NeoForge 的 {@code AddServerReloadListenersEvent} 上，因此<b>每次数据包重载都会重新装配</b>
 * ——这就是「改 {@code src/main/resources/gen/} 源 → 重跑 {@code runData} → 游戏内 {@code /reload}
 * 即生效、不需要重启」的实现方式（注册表做不到这件事，所以预设与形状改用了普通静态目录）。
 * <p>
 * 解析在 {@link #prepare}（后台线程）完成，{@link #apply}（主线程）只负责整体替换目录，
 * 这样世界生成 / 生长的读侧永远看到的是一个完整快照。
 */
public final class TreePresetCatalogLoader extends SimplePreparableReloadListener<Map<Identifier, TreePreset>> {

    /** 生成产物所在的 data 子目录；必须与 {@code WoodTreePresetProvider} 的输出保持一致。 */
    public static final String PRESET_DIR = "tree_preset";

    /**
     * 目录 → id 的转换器。<b>必须经它换算</b>：{@code ResourceManager.listResources} 返回的 id
     * <b>带目录前缀</b>（形如 {@code science_and_magic:tree_preset/fancy.json}），只有
     * {@link FileToIdConverter#fileToId} 才会把它变成 {@code science_and_magic:fancy}。
     * 这正是原版读配方 / 战利品表的方式（{@code SimpleJsonResourceReloadListener.scanDirectory}）。
     */
    private static final FileToIdConverter CONVERTER = FileToIdConverter.json(PRESET_DIR);

    @Override
    protected Map<Identifier, TreePreset> prepare(ResourceManager manager, ProfilerFiller profiler) {
        // 用「资源栈」而不是单份资源：栈首是优先级最高的那一份（数据包能覆盖 mod 自带的内容）。
        Map<Identifier, List<Resource>> files = CONVERTER.listMatchingResourceStacks(manager);

        Map<Identifier, TreePreset> parsed = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();

        files.forEach((file, stack) -> {
            Identifier presetId = CONVERTER.fileToId(file);

            // 防呆：换算后仍带斜杠说明前缀没有被剥掉（换算逻辑失效）。宁可当场报错，
            // 也不要装进一个永远查不到的键——那会让树苗静默不长，极难排查。
            if (presetId.getPath().contains("/")) {
                problems.add(file + "：换算出的预设 id " + presetId + " 仍带目录，前缀剥离异常");
                return;
            }
            if (stack.isEmpty()) {
                return;
            }
            if (stack.size() > 1) {
                ScienceAndMagic.LOGGER.warn("树形预设 {} 由 {} 个数据包提供，采用优先级最高的那一份（{}）",
                        presetId, stack.size(), stack.get(0).sourcePackId());
            }

            try (BufferedReader reader = stack.get(0).openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                parsed.put(presetId, TreePresetFile.parse(presetId, json));
            } catch (IOException | RuntimeException e) {
                // 单个文件坏掉不该让整份数据包加载失败：跳过它并明确报出来，
                // 其余预设照常可用（这也是「运行期只保留必要检测」的那一类）。
                problems.add(file + "：" + e.getMessage());
            }
        });

        for (String problem : problems) {
            ScienceAndMagic.LOGGER.error("树形预设文件有问题，已跳过：{}", problem);
        }
        return parsed;
    }

    @Override
    protected void apply(Map<Identifier, TreePreset> prepared, ResourceManager manager, ProfilerFiller profiler) {
        if (prepared.isEmpty()) {
            // 一个文件都没读到就不动现有目录：这可能是「真的没有预设」，也可能是 datagen 期间那次
            // 附带重载（此时 data/<ns>/tree_preset/ 尚未生成，而 datagen 自己已经装好了目录）。
            // 宁可保留旧的也不清空——清了会把正在同进程跑的 runData 弄坏，而报出来的 WARN 足够定位问题。
            ScienceAndMagic.LOGGER.warn("树形预设目录读不到任何文件（data/*/{}/*.json），保留当前已装载的 {} 个预设。"
                    + "如果这是首次启动，最常见的原因是没有跑过 `./gradlew runData`。", PRESET_DIR,
                    TreePresetCatalog.size());
            return;
        }

        TreePresetCatalog.install(prepared);
        for (TreePreset preset : prepared.values()) {
            ScienceAndMagic.LOGGER.info("树形预设 {}：{}（每区块 {} 次）", preset.id(), preset.describe(),
                    preset.placementCount());
        }
    }
}
