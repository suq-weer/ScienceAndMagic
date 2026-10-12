// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.PlacerTreeShape;
import team.xcmt.sam.api.wood.TemplateTreeShape;
import team.xcmt.sam.api.wood.TreePreset;
import team.xcmt.sam.api.wood.TreePresetCatalog;
import team.xcmt.sam.api.wood.TreeShape;
import team.xcmt.sam.api.wood.TreeShapes;

/**
 * 树形<b>手写源的加载器与唯一校验关卡</b>，读取 {@code src/main/resources/gen/tree_shapes/}
 * ——它与 {@code data/}、{@code assets/} 同级，下文简称 {@code gen/tree_shapes/}。
 * <p>
 * <b>目录约定</b>（{@code gen/tree_shapes/} 之下）：
 * <pre>
 * &lt;预设&gt;/_preset.json                     可选：placement_count 与 bucket_weights
 * &lt;预设&gt;/&lt;N&gt;x&lt;N&gt;/&lt;名称&gt;.nbt               NBT 蓝图（结构方块导出格式）
 * &lt;预设&gt;/&lt;N&gt;x&lt;N&gt;/&lt;名称&gt;@&lt;权重&gt;.nbt            同上，带权重（默认 1）
 * &lt;预设&gt;/&lt;N&gt;x&lt;N&gt;/&lt;内置形状&gt;.shape            引用 {@link TreeShapes} 里的代码几何
 * </pre>
 * 例如 {@code gen/tree_shapes/fancy/3x3/fancy_big_tree.nbt} 会得到预设 {@code science_and_magic:fancy}
 * 的 3x3 桶成员；{@code gen/tree_shapes/spruce/1x1/pine.shape} 则把内置的 {@code pine} 放进云杉预设的 1x1 桶。
 * <p>
 * <b>为什么源放在 {@code gen/} 而不是 {@code data/}</b>：它是 datagen 的<b>输入</b>而不是运行时的数据。
 * 原版资源系统只读 pack 根下的 {@code data/} 与 {@code assets/}，所以 {@code gen/} 既不会被当成数据包内容，
 * 又能随源码树被 IDE 与构建脚本直接看到。运行时消费的是 {@code runData} 产出的编译产物
 * （{@code data/<ns>/tree_preset/*.json} 与 {@code data/<ns>/structure/tree_shapes/**} 下的蓝图副本）。
 * <p>
 * <b>校验策略</b>：一次遍历<b>收集全部问题</b>再统一抛出，而不是遇到第一个就失败——
 * 否则改一个跑一次 {@code runData}，来回成本很高。
 */
public final class TreeSourceLoader {

    /** {@code gen/} 下的子目录名（保留这一层，是为了以后 {@code gen/} 还能放别类 datagen 源）。 */
    public static final String SHAPE_DIR = "tree_shapes";

    /** datagen 进程的工作目录不一定是项目根，因此由 {@code build.gradle} 注入绝对路径。 */
    private static final String GEN_DIR_PROPERTY = "sam.genDir";
    private static final String PRESET_CONFIG = "_preset.json";
    private static final Pattern CELL = Pattern.compile("^(\\d+)x(\\d+)$");

    /**
     * 一张需要复制进数据包的蓝图。
     * <p>
     * 直接带上 {@link TemplateTreeShape}：它已经同时持有「写哪个文件」（{@code templateFile()}）与
     * 「运行时用哪个 id」（{@code template()}），两者由同一个入参推出，因此不可能写这儿查那儿。
     */
    public record BlueprintCopy(TemplateTreeShape shape, byte[] bytes, String sourcePath) {
    }

    /** 装载结果：预设目录 + 待落盘的蓝图副本。 */
    public record Loaded(Map<Identifier, TreePreset> presets, List<BlueprintCopy> blueprints) {
    }

    private static Loaded loaded;

    private TreeSourceLoader() {
    }

    /**
     * 惰性装载（解析与校验只做一次），并把结果装进 {@link TreePresetCatalog}。
     * <p>
     * <b>每次调用都会重新装入目录</b>（幂等、开销只是 Map.copyOf）：datagen 环境里可能发生
     * 一次附带的数据包重载，而那时生成的 {@code tree_preset/} 还不存在，运行时的
     * {@code TreePresetCatalogLoader} 会装入空表。每次使用前重新装入，就不依赖任何执行顺序。
     * <p>
     * 任何校验不通过都抛 {@link IllegalStateException}，让 {@code runData} 直接失败。
     */
    public static synchronized Loaded load() {
        if (loaded == null) {
            loaded = doLoad();
        }
        TreePresetCatalog.install(loaded.presets());
        return loaded;
    }

    /** 拆开单个文件名用的中间结果。 */
    private record Stem(String name, int weight, String extension) {
    }

    // ------------------------------------------------------------------
    // 装载
    // ------------------------------------------------------------------

    private static Loaded doLoad() {
        Path root = resolveSourceRoot();
        Map<String, PresetBuilder> builders = new LinkedHashMap<>();
        List<BlueprintCopy> blueprints = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        HolderGetter<Block> blocks = BuiltInRegistries.BLOCK;

        try (Stream<Path> walk = Files.walk(root)) {
            // 排序只是为了让诊断信息的顺序稳定可读。
            for (Path file : walk.filter(Files::isRegularFile).sorted().toList()) {
                Path relative = root.relativize(file);
                String where = SHAPE_DIR + "/" + relative.toString().replace('\\', '/');
                if (relative.getNameCount() == 2 && PRESET_CONFIG.equals(relative.getName(1).toString())) {
                    readPresetConfig(relative.getName(0).toString(), file, builders, problems);
                } else if (relative.getNameCount() == 3) {
                    readBucketMember(relative.getName(0).toString(), relative.getName(1).toString(),
                            relative.getName(2).toString(), file, builders, blueprints, blocks, problems);
                } else {
                    problems.add(where + "：目录层级不符合约定（应为 <预设>/<N>x<N>/<文件>，或 <预设>/" + PRESET_CONFIG + "）");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("无法遍历树形源目录 " + root + "：" + e, e);
        }

        Map<Identifier, TreePreset> presets = new LinkedHashMap<>();
        builders.forEach((name, builder) -> {
            if (builder.isEmpty()) {
                problems.add(name + "：这个预设下没有任何形状；请在 " + name
                        + "/<N>x<N>/ 里放 .nbt 或 .shape，或者把这个目录删掉");
                return;
            }
            Identifier id = ScienceAndMagic.id(name);
            try {
                presets.put(id, builder.build());
            } catch (IllegalArgumentException e) {
                problems.add(name + "：预设自身校验失败 —— " + e.getMessage());
            }
        });

        if (!problems.isEmpty()) {
            throw new IllegalStateException("树形源校验失败（" + problems.size() + " 项），runData 中止：\n  - "
                    + String.join("\n  - ", problems));
        }
        if (presets.isEmpty()) {
            throw new IllegalStateException("树形源目录 " + root + " 下一个预设都没有；"
                    + "请按 <预设>/<N>x<N>/<名称>.nbt（或 .shape）的约定放文件");
        }

        for (TreePreset preset : presets.values()) {
            ScienceAndMagic.LOGGER.info("树形预设 {}：{}（每区块 {} 次）", preset.id(), preset.describe(),
                    preset.placementCount());
        }
        ScienceAndMagic.LOGGER.info("树形源 {}：{} 个预设、{} 个蓝图副本", root, presets.size(), blueprints.size());
        // 保留目录顺序（不用 Map.copyOf：它的迭代顺序带 JVM 级哈希扰动）——这样产物的写入顺序、
        // 地物里选择器的成员顺序在同一个源下永远一致，runData 才真正幂等。
        return new Loaded(Collections.unmodifiableMap(new LinkedHashMap<>(presets)), List.copyOf(blueprints));
    }

    /**
     * 定位手写源根目录。
     * <p>
     * 优先用 {@code build.gradle} 注入的绝对路径；没有该属性时从工作目录逐级向上找
     * {@code src/main/resources/gen/tree_shapes} —— datagen 的工作目录实测不是项目根，
     * 所以不能直接假设 {@code gen/} 就在当前目录。
     */
    private static Path resolveSourceRoot() {
        String configured = System.getProperty(GEN_DIR_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            Path root = Path.of(configured).resolve(SHAPE_DIR);
            if (Files.isDirectory(root)) {
                return root;
            }
            throw new IllegalStateException("系统属性 " + GEN_DIR_PROPERTY + "（=" + configured
                    + "）之下找不到 " + SHAPE_DIR + " 目录；请检查 build.gradle 里 data run 的 systemProperty");
        }

        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("src").resolve("main").resolve("resources").resolve("gen").resolve(SHAPE_DIR);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("找不到树形源目录 " + SHAPE_DIR + "：请在 build.gradle 的 data run 里注入 "
                + "-D" + GEN_DIR_PROPERTY + "=<项目根>/src/main/resources/gen，或确保工作目录在项目之下");
    }

    // ------------------------------------------------------------------
    // 单个文件
    // ------------------------------------------------------------------

    private static void readBucketMember(String preset, String cell, String fileName, Path file,
            Map<String, PresetBuilder> builders, List<BlueprintCopy> blueprints, HolderGetter<Block> blocks,
            List<String> problems) {
        String where = SHAPE_DIR + "/" + preset + "/" + cell + "/" + fileName;

        Matcher matcher = CELL.matcher(cell);
        if (!matcher.matches()) {
            problems.add(where + "：桶目录必须形如 <N>x<N>");
            return;
        }
        int sizeX = Integer.parseInt(matcher.group(1));
        int sizeZ = Integer.parseInt(matcher.group(2));
        if (sizeX != sizeZ) {
            problems.add(where + "：桶目录必须是正方形 <N>x<N>，收到 " + cell);
            return;
        }
        if (sizeX > TreePreset.MAX_MATRIX_SIZE) {
            problems.add(where + "：树苗矩阵边长上限是 " + TreePreset.MAX_MATRIX_SIZE + "x"
                    + TreePreset.MAX_MATRIX_SIZE + "，收到 " + cell);
            return;
        }

        Stem stem = parseStem(fileName);
        PresetBuilder builder = builders.computeIfAbsent(preset, PresetBuilder::new);

        switch (stem.extension()) {
            case "nbt" -> {
                byte[] bytes;
                try {
                    bytes = Files.readAllBytes(file);
                } catch (IOException e) {
                    problems.add(where + "：读取失败 —— " + e);
                    return;
                }
                StructureTemplate template;
                try {
                    template = BlueprintValidator.parse(where, bytes, blocks);
                    BlueprintValidator.checkFootprint(where, template, sizeX);
                } catch (BlueprintValidator.InvalidBlueprint e) {
                    problems.add(e.getMessage());
                    return;
                }
                // 形状路径 = <预设>/<N>x<N>/<名称>（天然避开跨预设重名）。
                // 形状 id、蓝图文件路径、蓝图模板 id 全部由它推出，见 TemplateTreeShape。
                TemplateTreeShape shape = TemplateTreeShape.of(ScienceAndMagic.MOD_ID,
                        preset + "/" + cell + "/" + stem.name(), sizeX);
                blueprints.add(new BlueprintCopy(shape, bytes, where));
                builder.add(sizeX, shape, stem.weight());
            }
            case "shape" -> {
                PlacerTreeShape builtin = TreeShapes.byPath(stem.name());
                if (builtin == null) {
                    problems.add(where + "：未知的内置形状 " + stem.name() + "；可用：" + String.join(", ", TreeShapes.paths()));
                    return;
                }
                if (builtin.size() != sizeX) {
                    problems.add(where + "：内置形状 " + stem.name() + " 需要 " + builtin.size() + "x" + builtin.size()
                            + " 的矩阵，不能放进 " + cell + " 桶");
                    return;
                }
                builder.add(sizeX, builtin, stem.weight());
            }
            default -> problems.add(where + "：只支持 .nbt（蓝图）与 .shape（引用内置形状）");
        }
    }

    private static void readPresetConfig(String preset, Path file, Map<String, PresetBuilder> builders,
            List<String> problems) {
        String where = SHAPE_DIR + "/" + preset + "/" + PRESET_CONFIG;
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            problems.add(where + "：读取失败 —— " + e);
            return;
        }

        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(text);
        } catch (RuntimeException e) {
            problems.add(where + "：不是合法 JSON —— " + e.getMessage());
            return;
        }
        if (!parsed.isJsonObject()) {
            problems.add(where + "：必须是一个 JSON 对象");
            return;
        }

        JsonObject json = parsed.getAsJsonObject();
        PresetBuilder builder = builders.computeIfAbsent(preset, PresetBuilder::new);

        json.entrySet().forEach(entry -> {
            switch (entry.getKey()) {
                case "placement_count" -> {
                    try {
                        int count = entry.getValue().getAsInt();
                        if (count < 0) {
                            problems.add(where + "：placement_count 不能为负（收到 " + count + "）");
                        } else {
                            builder.placementCount(count);
                        }
                    } catch (RuntimeException e) {
                        problems.add(where + "：placement_count 必须是整数");
                    }
                }
                case "bucket_weights" -> {
                    if (!entry.getValue().isJsonObject()) {
                        problems.add(where + "：bucket_weights 必须是一个对象，形如 {\"3\": 0.11}");
                        return;
                    }
                    entry.getValue().getAsJsonObject().entrySet().forEach(weightEntry -> {
                        try {
                            int size = Integer.parseInt(weightEntry.getKey());
                            double weight = weightEntry.getValue().getAsDouble();
                            if (weight <= 0.0) {
                                problems.add(where + "：bucket_weights[" + weightEntry.getKey() + "] 必须为正数");
                            } else {
                                builder.bucketWeight(size, weight);
                            }
                        } catch (NumberFormatException e) {
                            problems.add(where + "：bucket_weights 的键必须是整数边长，收到 " + weightEntry.getKey());
                        } catch (RuntimeException e) {
                            problems.add(where + "：bucket_weights[" + weightEntry.getKey() + "] 必须是数字");
                        }
                    });
                }
                // 不认识的键多半是拼写错误，直接报出来而不是静默忽略。
                default -> problems.add(where + "：不认识的字段 " + entry.getKey()
                        + "（只支持 placement_count 与 bucket_weights）");
            }
        });
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    /** 文件名拆成「名字 / 权重 / 扩展名」，权重写在 {@code @} 之后，缺省为 1。 */
    private static Stem parseStem(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String stem = dot < 0 ? fileName : fileName.substring(0, dot);
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);

        int weight = 1;
        int at = stem.lastIndexOf('@');
        if (at > 0) {
            try {
                weight = Math.max(1, Integer.parseInt(stem.substring(at + 1)));
                stem = stem.substring(0, at);
            } catch (NumberFormatException ignored) {
                // '@' 后不是数字，就把它当作名字的一部分。
            }
        }
        return new Stem(stem, weight, extension);
    }

    /** 包一层 {@link TreePreset.Builder}，额外记住「有没有加过形状」。 */
    private static final class PresetBuilder {

        private final TreePreset.Builder delegate;
        private boolean empty = true;

        private PresetBuilder(String name) {
            this.delegate = TreePreset.builder(ScienceAndMagic.id(name));
        }

        private void add(int size, TreeShape shape, int weight) {
            this.delegate.add(size, shape, weight);
            this.empty = false;
        }

        private void placementCount(int count) {
            this.delegate.placementCount(count);
        }

        private void bucketWeight(int size, double weight) {
            this.delegate.bucketWeight(size, weight);
        }

        private boolean isEmpty() {
            return this.empty;
        }

        private TreePreset build() {
            return this.delegate.build();
        }
    }
}
