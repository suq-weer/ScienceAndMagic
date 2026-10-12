// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam;

import net.minecraft.world.level.block.entity.BlockEntityTypes;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.content.CombatAttributes;
import team.xcmt.sam.content.CuriosSoulBones;
import team.xcmt.sam.content.ElementAttachmentSpec;
import team.xcmt.sam.content.ElementEffects;
import team.xcmt.sam.content.ModBlocks;
import team.xcmt.sam.content.ModCreativeTabs;
import team.xcmt.sam.content.ModFeatures;
import team.xcmt.sam.content.ModParticles;
import team.xcmt.sam.content.TestItems;
import team.xcmt.sam.content.TreePresetCatalogLoader;
import team.xcmt.sam.content.WoodTemplateFeature;
import team.xcmt.sam.datagen.DataGenerators;

/**
 * 注册总入口。由 {@link ScienceAndMagic} 在模组构造时调用。
 * <p>
 * 树形预设与形状<b>不在这里注册</b>：它们不是注册表条目，而是「{@code src/main/resources/gen/} 手写源
 * → {@code runData} 编译 → 数据包产物 → 运行时静态目录」这条链路，
 * 见 {@code TreeSourceLoader} / {@code TreePresetCatalogLoader}。
 */
public class Register {

    public static void init(IEventBus bus) {
        // 蓝图链路：自定义地物与结构处理器。
        // 注意这两个进的是<b>原版</b>注册表（Registries.FEATURE / STRUCTURE_PROCESSOR）：
        // configured_feature JSON 必须按名引用地物，结构处理器需要 codec，这是绕不开的。
        ModFeatures.FEATURES.register(bus);
        ModFeatures.PROCESSORS.register(bus);

        // 注册表挂到总线
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.ITEMS.register(bus);
        ModBlocks.ENTITIES.register(bus);
        ModCreativeTabs.TABS.register(bus);

        // 玩家战斗属性（须在 EntityAttributeModificationEvent 之前完成注册）
        CombatAttributes.ATTRIBUTES.register(bus);

        // 元素附着效果与「武器元素附着」数据组件
        ElementEffects.MOB_EFFECTS.register(bus);
        ElementAttachmentSpec.DATA_COMPONENTS.register(bus);

        // 伤害数字粒子类型（真正的粒子画法在客户端注册：provider + particle group）
        ModParticles.PARTICLE_TYPES.register(bus);

        // 创建魂骨 / 测试物品条目（物品会引用上面已挂载的注册表）
        CuriosSoulBones.init();
        TestItems.init();

        // 模组总线事件
        bus.addListener(Register::onCommonSetup);
        bus.addListener(Register::onAddBlockEntityBlocks);
        bus.addListener(CombatAttributes::onEntityAttributeModification);
        bus.addListener(DataGenerators::gatherDataClient);
        bus.addListener(DataGenerators::gatherDataServer);

        // 游戏总线事件（必须挂 NeoForge.EVENT_BUS）
        // 1) 树形预设目录：数据包每次重载时读 data/<ns>/tree_preset/*.json（runData 的产物）并整体替换。
        //    于是「改 gen/ 源 → 重跑 runData → 游戏内 /reload」即可生效，不需要重启游戏。
        NeoForge.EVENT_BUS.addListener(Register::onAddServerReloadListeners);
        // 2) 蓝图预热：纯性能措施（把首次读 NBT 与首次算锚点的开销挪出骨粉那一瞬间）。
        //    放在 ServerStartedEvent 是因为那时数据包（含预设目录）已经加载完成。
        NeoForge.EVENT_BUS.addListener(Register::onServerStarted);
    }

    private static void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(ScienceAndMagic.id("tree_preset_catalog"), new TreePresetCatalogLoader());
    }

    private static void onServerStarted(ServerStartedEvent event) {
        WoodTemplateFeature.preload(event.getServer());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER_INFO();
    }

    /**
     * 打印已注册木种。
     * <p>
     * 预设的「桶分布」不在这里打印：目录在 datagen 期由 {@code TreeSourceLoader}、
     * 运行期由 {@code TreePresetCatalogLoader} 各自装配并打印，只有那两个时点才拿得到内容。
     */
    private static void LOGGER_INFO() {
        ScienceAndMagic.LOGGER.info("Science And Magic: {} 木种已注册", WoodSet.all().size());
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            ScienceAndMagic.LOGGER.info("  - {}: 树形预设 {}｜地图色 {}", material.name(), material.treePreset(),
                    material.palette().describeMapColors());
        }
    }

    /**
     * 把模组的方块注入到原版的方块实体类型里。
     * <p>
     * 26.2 的告示牌/悬挂告示牌/货架都使用原版的 {@code BlockEntityType}，Mod 新增的同类方块必须
     * 通过该事件加入 valid blocks，否则放置时会因校验失败而丢失方块实体。
     */
    private static void onAddBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        for (WoodSet set : WoodSet.all()) {
            event.modify(BlockEntityTypes.SHELF, set.shelf().get());
            event.modify(BlockEntityTypes.SIGN, set.sign().get(), set.wallSign().get());
            event.modify(BlockEntityTypes.HANGING_SIGN, set.hangingSign().get(), set.wallHangingSign().get());
        }
    }
}
