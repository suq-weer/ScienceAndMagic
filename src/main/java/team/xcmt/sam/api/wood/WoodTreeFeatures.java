// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.WeightedRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.content.ModFeatures;
import team.xcmt.sam.content.WoodTemplateConfiguration;

/**
 * 木种世界生成：把预设里用到的每一种 {@link TreeShape} 注册成 {@link ConfiguredFeature}，
 * 再用一个「加权随机选择器」把它们合成为每个木种<b>唯一</b>的 {@link PlacedFeature}，
 * 最后由 NeoForge 的 {@code neoforge:add_features} biome modifier 注入
 * {@link WoodMaterial#biomeTags()} 指定的群系。
 * <p>
 * <b>为什么用随机选择器</b>：桶模型允许一个木种同时拥有 1x1、2x2、3x3…… 多种形状。
 * 若为每个形状各注册一个 placed feature 并各自计数，区块里会出现「每种形状各自尝试若干次」的叠加效果；
 * 而随机选择器的语义是<b>每次放置尝试从全部桶的并集里抽一个形状</b>——这正是需求要的
 * 「不限大小随机抽取」。抽样权重 = 形状权重 × 桶权重（默认 {@code 1/N²}），
 * 于是 3x3 巨树不会和 1x1 小树一样常见。
 * <p>
 * 桶的成员来自 {@link TreePresetCatalog} 里装配的 {@link TreePreset}（datagen 期由
 * {@code TreeSourceLoader} 读 {@code src/main/resources/gen/tree_shapes/} 装入）：
 * <ul>
 *     <li>{@link PlacerTreeShape} → 原版 {@code Feature.TREE} + 该木种的原木 / 树叶</li>
 *     <li>{@link TemplateTreeShape} → {@code science_and_magic:wood_template} + 蓝图与木种 id</li>
 * </ul>
 */
public final class WoodTreeFeatures {

    private WoodTreeFeatures() {
    }

    // ------------------------------------------------------------------
    // 注册键
    // ------------------------------------------------------------------

    /** 单个形状的 ConfiguredFeature 键，例如 {@code sequoia_tree_1x1_pine}。 */
    public static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(WoodMaterial material, TreeShape shape) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                Identifier.fromNamespaceAndPath(material.namespace(), featureName(material, shape)));
    }

    /** 随机选择器 ConfiguredFeature 的键，例如 {@code sequoia_trees}。 */
    public static ResourceKey<ConfiguredFeature<?, ?>> treesConfiguredKey(WoodMaterial material) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                Identifier.fromNamespaceAndPath(material.namespace(), material.name() + "_trees"));
    }

    /** 每个木种唯一的 PlacedFeature 键，例如 {@code sequoia_trees}。 */
    public static ResourceKey<PlacedFeature> placedKey(WoodMaterial material) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                Identifier.fromNamespaceAndPath(material.namespace(), material.name() + "_trees"));
    }

    /**
     * biome modifier 的注册键。
     * <p>
     * 一个木种可以为多个群系标签各注册一个 modifier，因此文件名为
     * {@code <wood>_trees}、{@code <wood>_trees_1}、{@code <wood>_trees_2}…
     *
     * @param index {@link WoodMaterial#biomeTags()} 中的下标
     */
    public static ResourceKey<BiomeModifier> biomeModifierKey(WoodMaterial material, int index) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                material.id(index == 0 ? "_trees" : "_trees_" + index));
    }

    /** 形状对应的地物名，例如 {@code sequoia_tree_1x1_pine}。 */
    public static String featureName(WoodMaterial material, TreeShape shape) {
        return material.name() + "_tree_" + shape.featureSuffix();
    }

    // ------------------------------------------------------------------
    // datagen bootstrap
    // ------------------------------------------------------------------

    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);

        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            TreePreset preset = presetOf(material);
            if (preset == null) {
                continue;
            }

            for (TreeShape shape : preset.allShapes()) {
                registerShape(context, set, shape);
            }

            // 随机选择器：每次放置尝试在「全部桶的并集」里按 形状权重 × 桶权重 抽一个形状。
            WeightedList.Builder<Holder<PlacedFeature>> selector = WeightedList.builder();
            for (var bucketEntry : preset.buckets().entrySet()) {
                int size = bucketEntry.getKey();
                for (Weighted<TreeShape> weighted : bucketEntry.getValue().unwrap()) {
                    Holder<ConfiguredFeature<?, ?>> configured =
                            features.getOrThrow(configuredKey(material, weighted.value()));
                    // inlinePlaced 返回的就已经是 Holder<PlacedFeature>（内部是 direct holder，会内联展开）。
                    selector.add(PlacementUtils.inlinePlaced(configured),
                            preset.generationWeight(size, weighted.weight()));
                }
            }
            FeatureUtils.register(context, treesConfiguredKey(material), Feature.WEIGHTED_RANDOM_SELECTOR,
                    new WeightedRandomFeatureConfiguration(selector.build()));
        }
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context,
            HolderGetter<ConfiguredFeature<?, ?>> features) {
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            TreePreset preset = presetOf(material);
            if (preset == null || preset.placementCount() <= 0) {
                continue;
            }
            Holder<ConfiguredFeature<?, ?>> trees = features.getOrThrow(treesConfiguredKey(material));
            PlacementUtils.register(context, placedKey(material), trees,
                    CountPlacement.of(preset.placementCount()),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                    PlacementUtils.filteredByBlockSurvival(set.sapling().get()),
                    BiomeFilter.biome());
        }
    }

    /**
     * 为每个木种把「该预设的全部形状」注入到它自己的群系标签里。
     * <p>
     * {@link WoodMaterial#biomeTags()} 为空则完全不注册 biome modifier（该木种不自然生成）；
     * 每个标签单独注册一个 modifier，因此 {@code biomes} 字段无需做 HolderSet 合并。
     */
    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context,
            HolderGetter<Biome> biomes, HolderGetter<PlacedFeature> placedFeatures) {
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            List<TagKey<Biome>> biomeTags = material.biomeTags();
            if (biomeTags.isEmpty()) {
                continue;
            }
            TreePreset preset = presetOf(material);
            if (preset == null || preset.placementCount() <= 0) {
                continue;
            }

            HolderSet<PlacedFeature> placedSet = HolderSet.direct(placedFeatures.getOrThrow(placedKey(material)));

            for (int index = 0; index < biomeTags.size(); index++) {
                // getOrThrow 会在标签不存在时抛错：数据生成阶段直接失败，比静默不生成更容易发现。
                HolderSet<Biome> biomeSet = biomes.getOrThrow(biomeTags.get(index));
                context.register(biomeModifierKey(material, index),
                        new BiomeModifiers.AddFeaturesBiomeModifier(biomeSet, placedSet,
                                GenerationStep.Decoration.VEGETAL_DECORATION));
            }
        }
    }

    // ------------------------------------------------------------------
    // 内部
    // ------------------------------------------------------------------

    /**
     * 取木种引用的预设。
     * <p>
     * 找不到时抛出异常而不是跳过：预设 id 写错（或 {@code src/main/resources/gen/tree_shapes/} 里没有对应目录）
     * 应该在数据生成阶段就炸掉，否则只会得到「树苗怎么也不长」这种极难排查的现象。
     */
    private static TreePreset presetOf(WoodMaterial material) {
        TreePreset preset = TreePresetCatalog.get(material.treePreset());
        if (preset == null) {
            if (TreePresetCatalog.isEmpty()) {
                throw new IllegalStateException("树形预设目录还是空的：木种 " + material.name() + " 引用了 "
                        + material.treePreset() + "，但没有任何预设被装载。"
                        + "请确认数据生成时先调用了 TreeSourceLoader.load()"
                        + "（它读 src/main/resources/gen/tree_shapes/）");
            }
            throw new IllegalStateException("木种 " + material.name() + " 引用了不存在的树形预设 "
                    + material.treePreset()
                    + "；请检查 src/main/resources/gen/tree_shapes/ 下是否有同名目录，改完后重跑 runData");
        }
        return preset;
    }

    private static void registerShape(BootstrapContext<ConfiguredFeature<?, ?>> context, WoodSet set, TreeShape shape) {
        WoodMaterial material = set.material();
        ResourceKey<ConfiguredFeature<?, ?>> key = configuredKey(material, shape);
        if (shape instanceof PlacerTreeShape placer) {
            FeatureUtils.register(context, key, Feature.TREE, placer.createConfiguration(set));
        } else if (shape instanceof TemplateTreeShape template) {
            FeatureUtils.register(context, key, ModFeatures.WOOD_TEMPLATE.get(),
                    new WoodTemplateConfiguration(template.template(), material.id(), template.rotations(),
                            template.size()));
        } else {
            throw new IllegalStateException("未知的形状实现：" + shape.getClass().getName());
        }
    }
}
