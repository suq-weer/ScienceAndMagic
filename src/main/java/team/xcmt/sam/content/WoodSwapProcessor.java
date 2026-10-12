package team.xcmt.sam.content;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.util.WoodBlockRemapper;

/**
 * 蓝图放置时的「换木种 + 跳过空气」处理器，注册为 {@code science_and_magic:wood_swap}。
 * <p>
 * 三件事，按优先顺序：
 * <ol>
 *     <li><b>空气 → 返回 {@code null}</b>：结构处理器返回 {@code null} 表示「这个位置什么都不放」，
 *         于是蓝图里的大面积空气等价于原版的<b>结构空位</b>，不会把原地形挖空；</li>
 *     <li>橡木系方块 → 换成该木种的对应方块（见 {@link WoodBlockRemapper}）；</li>
 *     <li>其余方块（石头、苔藓、藤蔓……）→ 原样保留，方便做树根与装饰。</li>
 * </ol>
 * 处理器通过原版 {@code StructurePlaceSettings} 的处理器链生效，因此旋转、镜像、方块实体 NBT
 * 等都由原版 {@code StructureTemplate} 统一处理，不需要自己写一套放置循环。
 */
public class WoodSwapProcessor implements StructureProcessor {

    public static final MapCodec<WoodSwapProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("wood").forGetter(processor -> processor.woodId))
            .apply(instance, WoodSwapProcessor::new));

    private final Identifier woodId;

    public WoodSwapProcessor(Identifier woodId) {
        this.woodId = woodId;
    }

    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos targetPosition,
            BlockPos referencePos, StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings,
            StructureTemplate template) {
        BlockState state = processedBlockInfo.state();
        if (state.isAir()) {
            // 返回 null 就是「此位置什么都不放」，等价于结构空位：蓝图里的大面积空气不会挖空原地形。
            return null;
        }
        WoodSet wood = WoodSet.byId(this.woodId);
        if (wood == null) {
            return processedBlockInfo;
        }
        BlockState replaced = WoodBlockRemapper.remap(state, wood);
        if (replaced == null || replaced == state) {
            return processedBlockInfo;
        }
        return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), replaced, processedBlockInfo.nbt());
    }

    @Override
    public MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }
}
