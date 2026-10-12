// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Strippable;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.api.wood.WoodSet;

/**
 * NeoForge 数据映射：26.2 中「斧头去皮」由 {@code neoforge:strippables} 数据映射驱动，
 * 这里为每个木种注册 原木 → 去皮原木、木 → 去皮木。
 */
public final class ModDataMapProvider extends DataMapProvider {

    public ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.@NonNull Provider provider) {
        for (WoodSet set : WoodSet.all()) {
            builder(NeoForgeDataMaps.STRIPPABLES)
                    .add(set.log().getKey(), new Strippable(set.strippedLog().get()), false)
                    .add(set.wood().getKey(), new Strippable(set.strippedWood().get()), false);
        }
    }
}
