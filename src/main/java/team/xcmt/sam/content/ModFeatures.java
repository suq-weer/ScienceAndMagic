// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.content;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import team.xcmt.sam.ScienceAndMagic;

/**
 * 蓝图链路用到的两个注册表条目。
 * <p>
 * 26.2 的 {@code Feature} 不再持有可被外部 dispatch 的 {@code codec()}——配置的「type」分派由
 * {@code ConfiguredFeature} 通过 {@code Feature::configuredCodec} 完成，所以自定义地物只需要
 * {@code super(配置的 Codec)} 就够了，不需要覆盖任何方法。
 */
public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ScienceAndMagic.MOD_ID);

    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, ScienceAndMagic.MOD_ID);

    /** {@code science_and_magic:wood_template} */
    public static final DeferredHolder<Feature<?>, WoodTemplateFeature> WOOD_TEMPLATE =
            FEATURES.register("wood_template", () -> new WoodTemplateFeature(WoodTemplateConfiguration.CODEC));

    /** {@code science_and_magic:wood_swap} */
    public static final DeferredHolder<MapCodec<? extends StructureProcessor>, MapCodec<WoodSwapProcessor>> WOOD_SWAP =
            PROCESSORS.register("wood_swap", () -> WoodSwapProcessor.CODEC);

    private ModFeatures() {
    }
}
