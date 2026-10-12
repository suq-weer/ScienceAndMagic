// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import team.xcmt.sam.api.wood.TreePreset;
import team.xcmt.sam.content.TreePresetFile;

/**
 * 把 {@link TreeSourceLoader} 装载并校验过的预设与蓝图落成数据包产物：
 * <ul>
 *     <li>{@code data/<namespace>/tree_preset/<预设>.json} —— 运行时据此装配
 *         {@code TreePresetCatalog}（见 {@link TreePresetFile} 的格式说明）；</li>
 *     <li>{@code data/<namespace>/structure/tree_shapes/<预设>/<N>x<N>/<名称>.nbt} —— 蓝图<b>字节副本</b>。
 *         必须是原样字节、且必须放在 {@code structure/} 之下：原版 {@code StructureTemplateManager}
 *         只认这一层目录，而 {@code src/main/resources/gen/} 不是任何 pack 根（它不在 {@code data/} 之下）。</li>
 * </ul>
 * 蓝图不做重编码（不去空气、不归一化）：空气跳过已由运行时的 {@code WoodSwapProcessor} 处理，
 * 保持字节原样才能让「源与产物」用 SHA1 直接对照。
 */
public final class WoodTreePresetProvider implements DataProvider {

    private final PackOutput output;

    public WoodTreePresetProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        TreeSourceLoader.Loaded loaded = TreeSourceLoader.load();

        PackOutput.PathProvider presetPaths = this.output.createPathProvider(PackOutput.Target.DATA_PACK, "tree_preset");
        Path dataRoot = this.output.getOutputFolder(PackOutput.Target.DATA_PACK);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (TreePreset preset : loaded.presets().values()) {
            JsonObject json = TreePresetFile.write(preset);
            futures.add(DataProvider.saveStable(cache, json, presetPaths.json(preset.id())));
        }
        for (TreeSourceLoader.BlueprintCopy copy : loaded.blueprints()) {
            // 目标路径由 TemplateTreeShape 推出（structure/tree_shapes/<形状路径>.nbt），
            // 与运行时 StructureTemplateManager 的查找规则同源，不会出现「写这儿、查那儿」。
            Path target = dataRoot.resolve(copy.shape().template().getNamespace())
                    .resolve(copy.shape().templateFile());
            futures.add(writeBytes(cache, target, copy));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * 蓝图是二进制，走 {@link CachedOutput#writeIfNeeded} 以保持幂等（内容没变就不重写、不产生无谓 diff）。
     * <p>
     * 哈希算法用 SHA-256 而非 SHA-1（Guava 已把 {@code sha1()} 标为过时）。{@code HashCache} 只把 SHA-1
     * 用在缓存目录名上，文件哈希原样采用调用方传入的值，因此这里换算法不影响缓存一致性。
     */
    private static CompletableFuture<?> writeBytes(CachedOutput cache, Path target,
            TreeSourceLoader.BlueprintCopy copy) {
        return CompletableFuture.runAsync(() -> {
            try {
                cache.writeIfNeeded(target, copy.bytes(), Hashing.sha256().hashBytes(copy.bytes()));
            } catch (IOException e) {
                throw new UncheckedIOException("写入蓝图副本失败：" + copy.sourcePath() + " -> " + target, e);
            }
        });
    }

    @Override
    public String getName() {
        return "Science And Magic 树形预设与蓝图副本";
    }
}
