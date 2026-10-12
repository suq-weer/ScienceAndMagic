// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.WoodTreeFeatures;

/**
 * 世界生成注册集：ConfiguredFeature / PlacedFeature / BiomeModifier。
 */
public final class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, ModWorldGenProvider::bootstrapPlaced)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::bootstrapBiomeModifiers);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(ScienceAndMagic.MOD_ID));
        // 构造期先装载并校验 src/main/resources/gen/tree_shapes/ 手写源：
        // 让「源有问题」在 provider 跑起来之前就暴露。
        TreeSourceLoader.load();
    }

    /**
     * 三个 bootstrap 各自在开头调一次 {@link TreeSourceLoader#load()}。
     * <p>
     * {@code load()} 幂等且每次都会重新装入目录，因此这里的作用是「不依赖执行顺序」：
     * datagen 内部可能发生一次附带的数据包重载，而那时生成的 {@code tree_preset/} 还不存在，
     * 运行时监听器会把目录换成空表；在使用点重新装入即可不受影响。
     */
    private static void bootstrapConfigured(net.minecraft.data.worldgen.BootstrapContext<ConfiguredFeature<?, ?>> context) {
        TreeSourceLoader.load();
        WoodTreeFeatures.bootstrapConfigured(context);
    }

    private static void bootstrapPlaced(net.minecraft.data.worldgen.BootstrapContext<PlacedFeature> context) {
        TreeSourceLoader.load();
        WoodTreeFeatures.bootstrapPlaced(context, context.lookup(Registries.CONFIGURED_FEATURE));
    }

    private static void bootstrapBiomeModifiers(net.minecraft.data.worldgen.BootstrapContext<BiomeModifier> context) {
        TreeSourceLoader.load();
        WoodTreeFeatures.bootstrapBiomeModifiers(context,
                context.lookup(Registries.BIOME),
                context.lookup(Registries.PLACED_FEATURE));
    }
}
