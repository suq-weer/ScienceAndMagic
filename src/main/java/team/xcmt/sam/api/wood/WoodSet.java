// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.api.wood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import team.xcmt.sam.content.WoodSaplingBlock;

/**
 * 通用「木种」封装：一个 {@link WoodSet} 代表一整套木头。
 * <p>
 * 对外只暴露 {@link #register}，内部完成：
 * <ul>
 *     <li>{@link BlockSetType} / {@link WoodType} 注册</li>
 *     <li>整套方块的注册（原木 / 木 / 去皮原木 / 去皮木 / 木板 / 楼梯 / 台阶 / 栅栏 / 栅栏门 /
 *         门 / 活板门 / 按钮 / 压力板 / 告示牌 / 墙告示牌 / 悬挂告示牌 / 墙悬挂告示牌 /
 *         货架 / 树叶 / 树苗）</li>
 *     <li>对应物品（含告示牌类特殊物品）</li>
 *     <li>船 / 箱船实体与船物品（当 {@link WoodMaterial#hasBoat()} 为真时）</li>
 * </ul>
 * 树苗是 {@link WoodSaplingBlock}：它按 {@link WoodMaterial#treePreset()} 指定的预设，
 * 支持任意 NxN 矩阵生长（原版 {@link TreeGrower} 只认 1x1 / 2x2，因此本模组只把它当作构造占位）。
 * 木种之间的外观差异完全由 {@link WoodMaterial#palette()} 的 OKLab 重着色参数决定：
 * 每个木种拥有自己的一套薄模型（{@code science_and_magic:block/wood/<木种>/*}），
 * 模型 {@code parent} 指向原版几何模板，{@code textures} 指向重着色后的 sprite。
 */
public final class WoodSet {

    /** 已注册的全部木种，供世界生成 / 数据生成遍历。 */
    private static final List<WoodSet> ALL = new ArrayList<>();

    private final WoodMaterial material;
    private final BlockSetType blockSetType;
    private final WoodType woodType;
    private final TreeGrower treeGrower;

    private final DeferredBlock<RotatedPillarBlock> log;
    private final DeferredBlock<RotatedPillarBlock> wood;
    private final DeferredBlock<RotatedPillarBlock> strippedLog;
    private final DeferredBlock<RotatedPillarBlock> strippedWood;
    private final DeferredBlock<Block> planks;
    private final DeferredBlock<StairBlock> stairs;
    private final DeferredBlock<SlabBlock> slab;
    private final DeferredBlock<FenceBlock> fence;
    private final DeferredBlock<FenceGateBlock> fenceGate;
    private final DeferredBlock<DoorBlock> door;
    private final DeferredBlock<TrapDoorBlock> trapdoor;
    private final DeferredBlock<ButtonBlock> button;
    private final DeferredBlock<PressurePlateBlock> pressurePlate;
    private final DeferredBlock<StandingSignBlock> sign;
    private final DeferredBlock<WallSignBlock> wallSign;
    private final DeferredBlock<CeilingHangingSignBlock> hangingSign;
    private final DeferredBlock<WallHangingSignBlock> wallHangingSign;
    private final DeferredBlock<ShelfBlock> shelf;
    private final DeferredBlock<UntintedParticleLeavesBlock> leaves;
    private final DeferredBlock<SaplingBlock> sapling;

    private final List<DeferredBlock<? extends Block>> blocks;
    private final List<DeferredItem<? extends Item>> items;
    private final Map<String, DeferredItem<? extends Item>> itemsBySuffix;
    private final Map<String, DeferredBlock<? extends Block>> blocksBySuffix;

    private DeferredHolder<EntityType<?>, EntityType<Boat>> boatEntity;
    private DeferredHolder<EntityType<?>, EntityType<ChestBoat>> chestBoatEntity;
    private DeferredItem<BoatItem> boatItem;
    private DeferredItem<BoatItem> chestBoatItem;

    private WoodSet(DeferredRegister.Blocks blockRegister, DeferredRegister.Items itemRegister,
            DeferredRegister.Entities entityRegister, WoodMaterial material) {
        this.material = material;

        // --- 木种类型（静态映射，非 Registry，需在数据包加载前完成）---
        this.blockSetType = BlockSetType.register(new BlockSetType(material.name()));
        this.woodType = WoodType.register(new WoodType(material.name(), this.blockSetType));
        this.treeGrower = emptyGrower(material);

        String n = material.name();

        // 地图颜色：每类方块自动取「该类别目标色」在 OKLab 空间下最接近的原版 MapColor
        // （见 WoodMapColors）。于是同一木种在地图上呈现它自己的色调，而不是原版橡木 / 植物的颜色；
        // 类别单独指定过色（如树叶）的方块会自然跟着那个色走。
        // 每个方块都用「它自己那类贴图」的颜色去折算：门 / 活板门 / 告示牌 / 悬挂告示牌 / 货架
        // 在原版各有独立贴图类别，因此单独给这些类别指定颜色时，地图色会跟着变，而不是一律跟木板走。
        MapColor planksMapColor = mapColor(WoodTexture.PLANKS);
        MapColor logMapColor = mapColor(WoodTexture.LOG);
        MapColor strippedMapColor = mapColor(WoodTexture.STRIPPED_LOG);
        MapColor doorMapColor = mapColor(WoodTexture.DOOR_BOTTOM);
        MapColor trapdoorMapColor = mapColor(WoodTexture.TRAPDOOR);
        MapColor signMapColor = mapColor(WoodTexture.SIGN);
        MapColor hangingSignMapColor = mapColor(WoodTexture.HANGING_SIGN);
        MapColor shelfMapColor = mapColor(WoodTexture.SHELF);
        MapColor leavesMapColor = mapColor(WoodTexture.LEAVES);
        MapColor saplingMapColor = mapColor(WoodTexture.SAPLING);

        // --- 方块 ---
        this.log = blockRegister.registerBlock(n + "_log", RotatedPillarBlock::new,
                p -> p.mapColor(logMapColor).instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
        this.wood = blockRegister.registerBlock(n + "_wood", RotatedPillarBlock::new,
                p -> p.mapColor(logMapColor).instrument(NoteBlockInstrument.BASS).strength(2.0F)
                        .sound(SoundType.WOOD).ignitedByLava());
        this.strippedLog = blockRegister.registerBlock("stripped_" + n + "_log", RotatedPillarBlock::new,
                p -> p.mapColor(strippedMapColor).instrument(NoteBlockInstrument.BASS).strength(2.0F)
                        .sound(SoundType.WOOD).ignitedByLava());
        this.strippedWood = blockRegister.registerBlock("stripped_" + n + "_wood", RotatedPillarBlock::new,
                p -> p.mapColor(strippedMapColor).instrument(NoteBlockInstrument.BASS).strength(2.0F)
                        .sound(SoundType.WOOD).ignitedByLava());
        this.planks = blockRegister.registerBlock(n + "_planks", Block::new,
                p -> plankProperties(planksMapColor, p));

        this.stairs = blockRegister.registerBlock(n + "_stairs",
                p -> new StairBlock(this.planks.get().defaultBlockState(), p),
                p -> plankProperties(planksMapColor, p));
        this.slab = blockRegister.registerBlock(n + "_slab", SlabBlock::new,
                p -> plankProperties(planksMapColor, p));
        this.fence = blockRegister.registerBlock(n + "_fence", FenceBlock::new,
                p -> plankProperties(planksMapColor, p).forceSolidOn());
        this.fenceGate = blockRegister.registerBlock(n + "_fence_gate",
                p -> new FenceGateBlock(this.woodType, p),
                p -> plankProperties(planksMapColor, p).forceSolidOn());
        this.door = blockRegister.registerBlock(n + "_door", p -> new DoorBlock(this.blockSetType, p),
                p -> p.mapColor(doorMapColor).strength(3.0F).noOcclusion()
                        .pushReaction(PushReaction.DESTROY).ignitedByLava());
        this.trapdoor = blockRegister.registerBlock(n + "_trapdoor", p -> new TrapDoorBlock(this.blockSetType, p),
                p -> p.mapColor(trapdoorMapColor).strength(3.0F).noOcclusion()
                        .isValidSpawn((state, level, pos, type) -> false)
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .pushReaction(PushReaction.DESTROY).ignitedByLava());
        this.button = blockRegister.registerBlock(n + "_button",
                p -> new ButtonBlock(this.blockSetType, 30, p),
                p -> p.mapColor(planksMapColor).noCollision().strength(0.5F)
                        .pushReaction(PushReaction.DESTROY));
        this.pressurePlate = blockRegister.registerBlock(n + "_pressure_plate",
                p -> new PressurePlateBlock(this.blockSetType, p),
                p -> p.mapColor(planksMapColor).noCollision().strength(0.5F).sound(SoundType.WOOD)
                        .pushReaction(PushReaction.DESTROY).ignitedByLava());
        this.sign = blockRegister.registerBlock(n + "_sign", p -> new StandingSignBlock(this.woodType, p),
                p -> signProperties(signMapColor, p));
        this.wallSign = blockRegister.registerBlock(n + "_wall_sign", p -> new WallSignBlock(this.woodType, p),
                p -> signProperties(signMapColor, p));
        this.hangingSign = blockRegister.registerBlock(n + "_hanging_sign",
                p -> new CeilingHangingSignBlock(this.woodType, p), p -> signProperties(hangingSignMapColor, p));
        this.wallHangingSign = blockRegister.registerBlock(n + "_wall_hanging_sign",
                p -> new WallHangingSignBlock(this.woodType, p), p -> signProperties(hangingSignMapColor, p));
        this.shelf = blockRegister.registerBlock(n + "_shelf", ShelfBlock::new,
                p -> p.mapColor(shelfMapColor).instrument(NoteBlockInstrument.BASS).sound(SoundType.SHELF)
                        .strength(2.0F, 3.0F).ignitedByLava());
        // 树叶贴图在重着色后已经是固定颜色，因此落叶粒子也必须用固定色：
        // TintedParticleLeavesBlock 会取生物群系叶色（随群系变化），与固定色的树叶不一致。
        int leafParticleColor = material.palette().targetColor(WoodTexture.LEAVES);
        this.leaves = blockRegister.registerBlock(n + "_leaves",
                p -> new UntintedParticleLeavesBlock(0.01F,
                        ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, leafParticleColor), p),
                p -> p.mapColor(leavesMapColor).strength(0.2F).randomTicks().sound(SoundType.GRASS)
                        .noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));
        this.sapling = blockRegister.registerBlock(n + "_sapling",
                p -> new WoodSaplingBlock(this.treeGrower, material.treePreset(), p),
                p -> p.mapColor(saplingMapColor).noCollision().randomTicks().instabreak().sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY));

        List<DeferredBlock<? extends Block>> blockList = new ArrayList<>();
        Collections.addAll(blockList, this.log, this.wood, this.strippedLog, this.strippedWood, this.planks,
                this.stairs, this.slab, this.fence, this.fenceGate, this.door, this.trapdoor, this.button,
                this.pressurePlate, this.sign, this.wallSign, this.hangingSign, this.wallHangingSign,
                this.shelf, this.leaves, this.sapling);
        this.blocks = List.copyOf(blockList);

        // 方块后缀表：与 itemsBySuffix 同构，供蓝图替换器（WoodBlockRemapper）按后缀取本方块的同类方块。
        // 特别注意它比物品表多了 wall_sign / wall_hanging_sign：蓝图里的墙上告示牌也要跟着换木种。
        Map<String, DeferredBlock<? extends Block>> blocksBySuffix = new LinkedHashMap<>();
        blocksBySuffix.put("log", this.log);
        blocksBySuffix.put("wood", this.wood);
        blocksBySuffix.put("stripped_log", this.strippedLog);
        blocksBySuffix.put("stripped_wood", this.strippedWood);
        blocksBySuffix.put("planks", this.planks);
        blocksBySuffix.put("stairs", this.stairs);
        blocksBySuffix.put("slab", this.slab);
        blocksBySuffix.put("fence", this.fence);
        blocksBySuffix.put("fence_gate", this.fenceGate);
        blocksBySuffix.put("door", this.door);
        blocksBySuffix.put("trapdoor", this.trapdoor);
        blocksBySuffix.put("button", this.button);
        blocksBySuffix.put("pressure_plate", this.pressurePlate);
        blocksBySuffix.put("sign", this.sign);
        blocksBySuffix.put("wall_sign", this.wallSign);
        blocksBySuffix.put("hanging_sign", this.hangingSign);
        blocksBySuffix.put("wall_hanging_sign", this.wallHangingSign);
        blocksBySuffix.put("shelf", this.shelf);
        blocksBySuffix.put("leaves", this.leaves);
        blocksBySuffix.put("sapling", this.sapling);
        this.blocksBySuffix = Map.copyOf(blocksBySuffix);

        // --- 物品（墙上的变体没有对应物品）---
        // 物品清单与「按后缀取物品」共用同一张表：装配完成后直接由它派生 this.items，
        // 因此船 / 箱船只需要往表里补两笔。
        Map<String, DeferredItem<? extends Item>> bySuffix = new LinkedHashMap<>();
        bySuffix.put("log", itemRegister.registerSimpleBlockItem(n + "_log", this.log));
        bySuffix.put("wood", itemRegister.registerSimpleBlockItem(n + "_wood", this.wood));
        bySuffix.put("stripped_log",
                itemRegister.registerSimpleBlockItem("stripped_" + n + "_log", this.strippedLog));
        bySuffix.put("stripped_wood",
                itemRegister.registerSimpleBlockItem("stripped_" + n + "_wood", this.strippedWood));
        bySuffix.put("planks", itemRegister.registerSimpleBlockItem(n + "_planks", this.planks));
        bySuffix.put("stairs", itemRegister.registerSimpleBlockItem(n + "_stairs", this.stairs));
        bySuffix.put("slab", itemRegister.registerSimpleBlockItem(n + "_slab", this.slab));
        bySuffix.put("fence", itemRegister.registerSimpleBlockItem(n + "_fence", this.fence));
        bySuffix.put("fence_gate", itemRegister.registerSimpleBlockItem(n + "_fence_gate", this.fenceGate));
        bySuffix.put("door", itemRegister.registerSimpleBlockItem(n + "_door", this.door));
        bySuffix.put("trapdoor", itemRegister.registerSimpleBlockItem(n + "_trapdoor", this.trapdoor));
        bySuffix.put("button", itemRegister.registerSimpleBlockItem(n + "_button", this.button));
        bySuffix.put("pressure_plate",
                itemRegister.registerSimpleBlockItem(n + "_pressure_plate", this.pressurePlate));
        bySuffix.put("shelf", itemRegister.registerSimpleBlockItem(n + "_shelf", this.shelf));
        bySuffix.put("leaves", itemRegister.registerSimpleBlockItem(n + "_leaves", this.leaves));
        bySuffix.put("sapling", itemRegister.registerSimpleBlockItem(n + "_sapling", this.sapling));
        bySuffix.put("sign", itemRegister.registerItem(n + "_sign",
                p -> new SignItem(this.sign.get(), this.wallSign.get(), p), p -> p.stacksTo(16)));
        bySuffix.put("hanging_sign", itemRegister.registerItem(n + "_hanging_sign",
                p -> new HangingSignItem(this.hangingSign.get(), this.wallHangingSign.get(), p),
                p -> p.stacksTo(16)));

        // --- 船 / 箱船 ---
        if (material.hasBoat()) {
            // 注意：entity type 注册表在 item 之前完成注册（BuiltInRegistries 顺序），
            // 因此这里在物品创建时 boatEntity 已经可用。
            this.boatEntity = entityRegister.registerEntityType(n + "_boat",
                    (type, level) -> new Boat(type, level, () -> this.boatItem.get()),
                    MobCategory.MISC,
                    b -> b.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
            this.chestBoatEntity = entityRegister.registerEntityType(n + "_chest_boat",
                    (type, level) -> new ChestBoat(type, level, () -> this.chestBoatItem.get()),
                    MobCategory.MISC,
                    b -> b.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
            this.boatItem = itemRegister.registerItem(n + "_boat",
                    p -> new BoatItem(this.boatEntity.get(), p), p -> p.stacksTo(1));
            this.chestBoatItem = itemRegister.registerItem(n + "_chest_boat",
                    p -> new BoatItem(this.chestBoatEntity.get(), p), p -> p.stacksTo(1));
            bySuffix.put("boat", this.boatItem);
            bySuffix.put("chest_boat", this.chestBoatItem);
        }

        this.items = List.copyOf(bySuffix.values());
        this.itemsBySuffix = bySuffix;
        ALL.add(this);
    }

    /** 注册一个新木种。 */
    public static WoodSet register(DeferredRegister.Blocks blockRegister, DeferredRegister.Items itemRegister,
            DeferredRegister.Entities entityRegister, WoodMaterial material) {
        return new WoodSet(blockRegister, itemRegister, entityRegister, material);
    }

    /** 已注册的全部木种。 */
    public static List<WoodSet> all() {
        return List.copyOf(ALL);
    }

    /** 树苗方块 → 木种 的反查表；延迟构建，因为方块只有注册完成后才能解析。 */
    private static final Map<Block, WoodSet> BY_SAPLING = new HashMap<>();
    private static boolean saplingIndexBuilt;

    /**
     * 按树苗方块反查它属于哪个木种。
     * <p>
     * 树苗生长时只知道「自己这个方块」，而形状放置需要木种来注入原木 / 树叶，
     * 因此用这张表把两者接起来（与 {@link #byId(Identifier)} 同一思路）。
     *
     * @return 不属于任何木种时返回 {@code null}
     */
    public static WoodSet bySapling(Block sapling) {
        if (!saplingIndexBuilt) {
            saplingIndexBuilt = true;
            for (WoodSet set : ALL) {
                BY_SAPLING.put(set.sapling().get(), set);
            }
        }
        return BY_SAPLING.get(sapling);
    }

    /**
     * 按木种 ID（{@code <namespace>:<name>}）查找已注册的木种。
     * <p>
     * 运行时 sprite source 只能从 JSON 里读到字符串，因此需要这个反查。
     *
     * @return 未注册时返回 {@code null}
     */
    public static WoodSet byId(Identifier id) {
        for (WoodSet set : ALL) {
            if (set.material().id().equals(id)) {
                return set;
            }
        }
        return null;
    }

    /**
     * 树苗基类 {@link SaplingBlock} 的构造要求一个 {@link TreeGrower}，但本模组的生长逻辑已经完全由
     * {@link WoodSaplingBlock#advanceTree} 接管（原版只认 1x1 / 2x2，而我们要支持任意 NxN 矩阵）。
     * <p>
     * 这里给一个「什么都不长」的空 grower，只用来满足基类构造；{@code SaplingBlock.isValidBonemealTarget}
     * 会读它的 {@code getMinimumHeight().orElse(0)}，空 grower 返回 {@code OptionalInt.empty()}，
     * 因此退化成「不限制高度」，是安全值。
     */
    private static TreeGrower emptyGrower(WoodMaterial material) {
        return new TreeGrower(material.name(), 0.0F,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    /**
     * 某类别方块使用的地图颜色。
     * <p>
     * 由调色板统一折算（见 {@link WoodPalette#resolveMapColor(WoodTexture)}）：取该类别目标色在
     * OKLab 空间下最接近的原版 {@link MapColor}，于是同一木种在地图上呈现它自己的色调，
     * 而不是原版橡木 / 植物的颜色；类别单独指定过色（如树叶）的方块会自然跟着那个色走。
     */
    private MapColor mapColor(WoodTexture texture) {
        return this.material.palette().resolveMapColor(texture);
    }

    private static BlockBehaviour.Properties plankProperties(MapColor mapColor, BlockBehaviour.Properties p) {
        return p.mapColor(mapColor).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
                .sound(SoundType.WOOD).ignitedByLava();
    }

    private static BlockBehaviour.Properties signProperties(MapColor mapColor, BlockBehaviour.Properties p) {
        return p.mapColor(mapColor).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision()
                .strength(1.0F).ignitedByLava();
    }

    // ------------------------------------------------------------------
    // getters
    // ------------------------------------------------------------------

    public WoodMaterial material() {
        return this.material;
    }

    public BlockSetType blockSetType() {
        return this.blockSetType;
    }

    public WoodType woodType() {
        return this.woodType;
    }

    public TreeGrower treeGrower() {
        return this.treeGrower;
    }

    public DeferredBlock<RotatedPillarBlock> log() {
        return this.log;
    }

    public DeferredBlock<RotatedPillarBlock> wood() {
        return this.wood;
    }

    public DeferredBlock<RotatedPillarBlock> strippedLog() {
        return this.strippedLog;
    }

    public DeferredBlock<RotatedPillarBlock> strippedWood() {
        return this.strippedWood;
    }

    public DeferredBlock<Block> planks() {
        return this.planks;
    }

    public DeferredBlock<StairBlock> stairs() {
        return this.stairs;
    }

    public DeferredBlock<SlabBlock> slab() {
        return this.slab;
    }

    public DeferredBlock<FenceBlock> fence() {
        return this.fence;
    }

    public DeferredBlock<FenceGateBlock> fenceGate() {
        return this.fenceGate;
    }

    public DeferredBlock<DoorBlock> door() {
        return this.door;
    }

    public DeferredBlock<TrapDoorBlock> trapdoor() {
        return this.trapdoor;
    }

    public DeferredBlock<ButtonBlock> button() {
        return this.button;
    }

    public DeferredBlock<PressurePlateBlock> pressurePlate() {
        return this.pressurePlate;
    }

    public DeferredBlock<StandingSignBlock> sign() {
        return this.sign;
    }

    public DeferredBlock<WallSignBlock> wallSign() {
        return this.wallSign;
    }

    public DeferredBlock<CeilingHangingSignBlock> hangingSign() {
        return this.hangingSign;
    }

    public DeferredBlock<WallHangingSignBlock> wallHangingSign() {
        return this.wallHangingSign;
    }

    public DeferredBlock<ShelfBlock> shelf() {
        return this.shelf;
    }

    public DeferredBlock<UntintedParticleLeavesBlock> leaves() {
        return this.leaves;
    }

    public DeferredBlock<SaplingBlock> sapling() {
        return this.sapling;
    }

    public DeferredHolder<EntityType<?>, EntityType<Boat>> boatEntity() {
        return this.boatEntity;
    }

    public DeferredHolder<EntityType<?>, EntityType<ChestBoat>> chestBoatEntity() {
        return this.chestBoatEntity;
    }

    public DeferredItem<BoatItem> boatItem() {
        return this.boatItem;
    }

    public DeferredItem<BoatItem> chestBoatItem() {
        return this.chestBoatItem;
    }

    public List<DeferredBlock<? extends Block>> blockHolders() {
        return this.blocks;
    }

    public List<DeferredItem<? extends Item>> itemHolders() {
        return this.items;
    }

    /** 所有方块（已解析）。 */
    public List<Block> blocks() {
        List<Block> out = new ArrayList<>(this.blocks.size());
        for (DeferredBlock<? extends Block> holder : this.blocks) {
            out.add(holder.get());
        }
        return out;
    }

    /** 所有物品（已解析）。 */
    public List<Item> items() {
        List<Item> out = new ArrayList<>(this.items.size());
        for (DeferredItem<? extends Item> holder : this.items) {
            out.add(holder.get());
        }
        return out;
    }

    /**
     * 按规范后缀取物品。可用后缀：
     * {@code log / wood / stripped_log / stripped_wood / planks / stairs / slab / fence / fence_gate /
     * door / trapdoor / button / pressure_plate / shelf / leaves / sapling / sign / hanging_sign /
     * boat / chest_boat}。
     */
    public DeferredItem<? extends Item> item(String suffix) {
        return this.itemsBySuffix.get(suffix);
    }

    /** 按规范后缀取物品注册键（用于标签）。 */
    public ResourceKey<Item> itemKey(String suffix) {
        DeferredItem<? extends Item> item = this.itemsBySuffix.get(suffix);
        return item == null ? null : item.getKey();
    }

    /**
     * 按规范后缀取方块（已解析）。可用后缀：
     * {@code log / wood / stripped_log / stripped_wood / planks / stairs / slab / fence / fence_gate /
     * door / trapdoor / button / pressure_plate / sign / wall_sign / hanging_sign / wall_hanging_sign /
     * shelf / leaves / sapling}。
     * <p>
     * 与 {@link #item(String)} 的差别是多了 {@code wall_sign} / {@code wall_hanging_sign}
     * ——墙上变体没有物品，但蓝图里会出现。
     *
     * @return 没有该后缀时返回 {@code null}
     */
    public Block block(String suffix) {
        DeferredBlock<? extends Block> holder = this.blocksBySuffix.get(suffix);
        return holder == null ? null : holder.get();
    }
}
