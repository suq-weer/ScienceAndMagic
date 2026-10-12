// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.api.wood.WoodTexture;
import team.xcmt.sam.client.OklabRecolorSpriteSource;

/**
 * 生成图集定义 {@code assets/minecraft/atlases/blocks.json} 与 {@code items.json}。
 * <p>
 * 每个木种的每个图集类别都会产出一条 {@code science_and_magic:oklab_recolor} 源，
 * 把原版贴图（{@code minecraft:block/oak_planks} 之类）在装配图集时重着色成
 * {@code science_and_magic:block/wood/<木种>/<类别>}。
 * <p>
 * 注意写入的是 <b>{@code minecraft} 命名空间</b>：{@code SpriteSourceList} 用
 * {@code FileToIdConverter.idToFile(atlasId)} 推导文件名，命名空间随 atlas ID 走，
 * 而 blocks/items 图集都属于 {@code minecraft}。跨命名空间写同名文件是<b>叠加</b>而非覆盖
 * （{@code SpriteSourceList.load} 遍历 {@code getResourceStack} 合并所有资源包），
 * 所以原版图集的内容不会丢。
 * <p>
 * 本类引用了客户端类，因此只能在 {@code clientData} 运行中注册（见 {@link DataGenerators}）。
 */
public final class WoodAtlasProvider extends SpriteSourceProvider {

    public WoodAtlasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void gather() {
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            for (WoodTexture texture : WoodTexture.atlasValues()) {
                Identifier atlas = texture.target() == WoodTexture.Target.BLOCK_ATLAS
                        ? AtlasIds.BLOCKS
                        : AtlasIds.ITEMS;
                atlas(atlas).addSource(new OklabRecolorSpriteSource(
                        material.id(),
                        texture.key(),
                        texture.sourceId(material.baseNamespace(), material.basePrefix()),
                        material.sprite(texture)));
            }
        }
    }
}
