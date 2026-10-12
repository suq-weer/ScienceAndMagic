// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import team.xcmt.sam.api.wood.TemplateAnchors;

/**
 * 蓝图校验。**只在 datagen 期运行**——它让 {@code runData} 成为唯一把关点：
 * 能通过这里，运行时就不会再出现「蓝图读不出来」「锚点对不上」这类只能靠肉眼发现的问题。
 * <p>
 * 三项检查：
 * <ol>
 *     <li>能按结构蓝图（StructureTemplate NBT）解析；</li>
 *     <li>含木质标记方块（{@link TemplateAnchors#TRUNK_MARKERS}）——否则锚点无从确定；</li>
 *     <li>树干足迹容得下 NxN（见 {@link TemplateAnchors}）。</li>
 * </ol>
 */
final class BlueprintValidator {

    /** 校验不通过。消息面向「放文件的人」，必须能照着直接改。 */
    static final class InvalidBlueprint extends RuntimeException {

        InvalidBlueprint(String message) {
            super(message);
        }
    }

    private BlueprintValidator() {
    }

    /**
     * 把字节解析成结构蓝图。
     *
     * @param where  人可读的来源路径（写进错误消息）
     * @param blocks 方块查询表；datagen 期用 {@link BuiltInRegistries#BLOCK} 即可
     *               （{@code Registry extends HolderLookup.RegistryLookup extends HolderGetter}）
     */
    static StructureTemplate parse(String where, byte[] bytes, HolderGetter<Block> blocks) {
        try {
            CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
            StructureTemplate template = new StructureTemplate();
            template.load(blocks, tag);
            if (template.getSize().equals(Vec3i.ZERO)) {
                throw new InvalidBlueprint(where + "：蓝图是空的（尺寸是 0x0x0）");
            }
            return template;
        } catch (IOException | RuntimeException e) {
            if (e instanceof InvalidBlueprint invalid) {
                throw invalid;
            }
            throw new InvalidBlueprint(where + "：无法解析为结构蓝图（NBT 可能损坏，或不是结构方块导出的格式）：" + e);
        }
    }

    /**
     * 检查树干足迹容得下 {@code matrixSize x matrixSize}。
     * <p>
     * 这里把「足迹不匹配」当<b>硬错误</b>而不是警告：形状是启动时按桶目录挂到 N 上的，
     * 足迹与桶不一致意味着这棵树放进去必然错位，属于作者该改的数据问题，不该带病进游戏。
     */
    static void checkFootprint(String where, StructureTemplate template, int matrixSize) {
        TemplateAnchors.TrunkBase base = TemplateAnchors.trunkBase(template, matrixSize);

        if (base.noMarkerAtAll()) {
            throw new InvalidBlueprint(where + "：没有识别到任何木质标记方块，锚点无法确定。请用这些方块标记树干："
                    + markerNames());
        }
        if (!base.matched()) {
            throw new InvalidBlueprint(where + "：树干足迹容不下 " + matrixSize + "x" + matrixSize
                    + "。自最低层起各层的最大实心木质方格为 " + base.layerWidest()
                    + "（全图最大 " + base.widestSquare() + "x" + base.widestSquare() + "）。"
                    + "请确认树干最低那一层是一整块 " + matrixSize + "x" + matrixSize + " 的木质方块（不能空心 / 缺角 / L 形），"
                    + "或者把这张蓝图移到与它树干足迹相符的 <N>x<N> 桶目录下");
        }
    }

    /** 木质标记方块的可读列表，用于错误提示。 */
    static String markerNames() {
        List<String> names = TemplateAnchors.TRUNK_MARKERS.stream()
                .map(block -> BuiltInRegistries.BLOCK.getKey(block).toString())
                .collect(Collectors.toList());
        return String.join(" / ", names);
    }
}
