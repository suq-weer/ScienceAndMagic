// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.content;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.TemplateAnchors;
import team.xcmt.sam.api.wood.TemplateTreeShape;
import team.xcmt.sam.api.wood.TreePreset;
import team.xcmt.sam.api.wood.TreePresetCatalog;
import team.xcmt.sam.api.wood.TreeShape;

/**
 * 蓝图放置用的自定义地物，注册为 {@code science_and_magic:wood_template}。
 * <p>
 * 与原版 {@code Feature.TEMPLATE} 的区别只有两点，但都是必须的：
 * <ol>
 *     <li><b>挂上 {@link WoodSwapProcessor}</b>：按木种替换橡木系方块，并把空气当结构空位跳过；</li>
 *     <li><b>锚点按「树干足迹」匹配</b>（见 {@link TemplateAnchors}）：原版会把模板按 {@code size / 2}
 *         居中，而蓝图是手工搭的，树干几乎不可能正好在包围盒正中。这里改为把蓝图里
 *         「最低那一层存在完整 NxN 实心木质方块」的西北角对齐到树苗矩阵的西北角。</li>
 * </ol>
 * 蓝图内容由原版 {@code StructureTemplateManager} 负责加载与缓存（{@code /reload} 会清空缓存并重新读取），
 * 因此这里不自己维护模板内容缓存。
 * <p>
 * <b>本类在运行期只做防御，不做校验</b>：合法性（能解析、有木质标记、足迹匹配 NxN）已经全部前移到
 * {@code runData}。运行期还要兜的是「数据包覆盖 / 删除了蓝图」这种只有运行时才会出现的情况
 * ——它们必须被兜住，因为放置发生在骨粉催熟与随机刻的调用链上，异常逃出去会打断整段游戏逻辑。
 */
public class WoodTemplateFeature extends Feature<WoodTemplateConfiguration> {

    /**
     * 放置时的写块标志：{@code UPDATE_NEIGHBORS | UPDATE_CLIENTS}。
     * <p>
     * 带邻接更新是必须的——树叶的 {@code distance} 由邻接更新触发重算，
     * 否则蓝图树叶会因为「不连接原木」而在随机刻里迅速消失。
     */
    private static final int UPDATE_FLAGS = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;

    /** 已经报过「蓝图不可用」的 id，避免每次放置都刷日志。 */
    private static final Set<Identifier> REPORTED = ConcurrentHashMap.newKeySet();

    public WoodTemplateFeature(Codec<WoodTemplateConfiguration> codec) {
        super(codec);
    }

    /**
     * 服务器启动完成后预热：把预设用到的蓝图都加载一遍，并顺手算出锚点（锚点按模板实例缓存）。
     * <p>
     * <b>这是纯性能措施，不做校验</b>——校验已经在 {@code runData} 完成。它只是把「首次读 NBT +
     * 首次算锚点」的开销从骨粉催熟的那一瞬间挪到启动阶段，避免种树时出现一次卡顿。
     * 蓝图仍可能被数据包覆盖成坏的，那种情况由 {@link #place} 的防御分支给出一次性 ERROR。
     */
    public static void preload(MinecraftServer server) {
        StructureTemplateManager manager = server.getStructureManager();
        int warmed = 0;

        for (TreePreset preset : TreePresetCatalog.all()) {
            for (TreeShape shape : preset.allShapes()) {
                if (!(shape instanceof TemplateTreeShape template)) {
                    continue;
                }
                try {
                    StructureTemplate loaded = manager.getOrCreate(template.template());
                    if (loaded.getSize().equals(Vec3i.ZERO)) {
                        continue;
                    }
                    // 顺手把锚点算出来填进缓存；结果本身不需要，真正的消费在 place()。
                    TemplateAnchors.trunkBase(loaded, shape.size());
                    warmed++;
                } catch (RuntimeException ignored) {
                    // 静默：真正的放置会走 place() 的防御分支并给出可操作的 ERROR。
                }
            }
        }

        if (warmed > 0) {
            ScienceAndMagic.LOGGER.info("树形蓝图：已预热 {} 张（锚点同时进入缓存）", warmed);
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<WoodTemplateConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        WoodTemplateConfiguration config = context.config();

        ServerLevel serverLevel = level.getLevel();
        if (serverLevel == null || serverLevel.getServer() == null) {
            return false;
        }

        StructureTemplateManager manager = serverLevel.getStructureManager();
        StructureTemplate template;
        try {
            template = manager.getOrCreate(config.template());
        } catch (RuntimeException e) {
            // 蓝图 NBT 损坏、gzip 不完整、或字节序不对时，原版会抛 ReportedNbtException。
            // 这里必须兜住：放置发生在骨粉催熟 / 随机刻的调用链上，异常逃出去会打断整段游戏逻辑。
            reportOnce(config.template(), "蓝图文件无法解析（NBT 可能损坏），已跳过这个形状", e);
            return false;
        }

        if (template.getSize().equals(Vec3i.ZERO)) {
            // 直接把「期望的文件位置」打出来：这类失败几乎都是「id 与文件对不上」，
            // 有了期望路径就能一眼看出该把 .nbt 放哪儿（或 id 是不是写错了）。
            reportOnce(config.template(),
                    "找不到蓝图（或它是空的）；期望文件位置 " + TemplateTreeShape.expectedFile(config.template()), null);
            return false;
        }

        Rotation rotation = Util.getRandom(config.rotations(), random);
        BlockPos origin = context.origin();
        // 把蓝图的「树干基底西北角」挪到原点，于是它正好落在树苗矩阵的西北角上。
        BlockPos position = origin.subtract(TemplateAnchors.trunkBase(template, config.size()).anchor().rotate(rotation));

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setRandom(random)
                // 树不该被水浸；顺带省掉 placeInWorld 里逐块的 getFluidState 与流体回流 BFS。
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)
                .addProcessor(new WoodSwapProcessor(config.wood()));

        try {
            return template.placeInWorld(level, position, position, settings, random, UPDATE_FLAGS);
        } catch (RuntimeException e) {
            reportOnce(config.template(), "放置蓝图时出错，已跳过", e);
            return false;
        }
    }

    private static void reportOnce(Identifier template, String message, RuntimeException cause) {
        if (REPORTED.add(template)) {
            if (cause == null) {
                ScienceAndMagic.LOGGER.error("树形蓝图 {}：{}", template, message);
            } else {
                ScienceAndMagic.LOGGER.error("树形蓝图 {}：{}", template, message, cause);
            }
        }
    }
}
