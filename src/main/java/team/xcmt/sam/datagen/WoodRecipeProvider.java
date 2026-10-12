// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import net.minecraft.data.PackOutput;

/**
 * 生成合成配方；沿用原版同款配方形状（模板见 {@link WoodRecipeTemplates}）。
 */
public final class WoodRecipeProvider extends TemplatedJsonProvider {

    public WoodRecipeProvider(PackOutput output) {
        super(output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe"),
                WoodRecipeTemplates.TEMPLATES, "Wood Recipes");
    }
}
