package team.xcmt.sam.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;

/**
 * 方块标签：加入原版的 logs / planks / wooden_* / signs / leaves 等标签，
 * 并生成木种自己的 {@code <ns>:<wood>_logs} 标签（供木板配方使用）。
 */
public final class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            String modId) {
        super(output, lookupProvider, modId);
    }

    // IntrinsicTagAppender#add(T...) 对泛型元素会创建泛型数组，javac 会报不经检查的转换；
    // 这里传入的始终是 ResourceKey<Block>，转换是安全的。
    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            TagKey<Block> logs = TagKey.create(Registries.BLOCK, material.id("_logs"));

            ResourceKey<Block> log = set.log().getKey();
            ResourceKey<Block> wood = set.wood().getKey();
            ResourceKey<Block> strippedLog = set.strippedLog().getKey();
            ResourceKey<Block> strippedWood = set.strippedWood().getKey();
            ResourceKey<Block> planks = set.planks().getKey();
            ResourceKey<Block> stairs = set.stairs().getKey();
            ResourceKey<Block> slab = set.slab().getKey();
            ResourceKey<Block> fence = set.fence().getKey();
            ResourceKey<Block> fenceGate = set.fenceGate().getKey();
            ResourceKey<Block> door = set.door().getKey();
            ResourceKey<Block> trapdoor = set.trapdoor().getKey();
            ResourceKey<Block> button = set.button().getKey();
            ResourceKey<Block> pressurePlate = set.pressurePlate().getKey();
            ResourceKey<Block> sign = set.sign().getKey();
            ResourceKey<Block> wallSign = set.wallSign().getKey();
            ResourceKey<Block> hangingSign = set.hangingSign().getKey();
            ResourceKey<Block> wallHangingSign = set.wallHangingSign().getKey();
            ResourceKey<Block> shelf = set.shelf().getKey();
            ResourceKey<Block> leaves = set.leaves().getKey();

            tag(BlockTags.LOGS).add(log, wood, strippedLog, strippedWood);
            tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(log);
            tag(logs).add(log, wood, strippedLog, strippedWood);

            tag(Tags.Blocks.STRIPPED_LOGS).add(strippedLog);
            tag(Tags.Blocks.STRIPPED_WOODS).add(strippedWood);

            tag(BlockTags.PLANKS).add(planks);
            tag(BlockTags.WOODEN_STAIRS).add(stairs);
            tag(BlockTags.WOODEN_SLABS).add(slab);
            tag(BlockTags.WOODEN_FENCES).add(fence);
            tag(BlockTags.FENCE_GATES).add(fenceGate);
            tag(BlockTags.WOODEN_DOORS).add(door);
            tag(BlockTags.WOODEN_TRAPDOORS).add(trapdoor);
            tag(BlockTags.WOODEN_BUTTONS).add(button);
            tag(BlockTags.WOODEN_PRESSURE_PLATES).add(pressurePlate);
            tag(BlockTags.STANDING_SIGNS).add(sign);
            tag(BlockTags.WALL_SIGNS).add(wallSign);
            tag(BlockTags.CEILING_HANGING_SIGNS).add(hangingSign);
            tag(BlockTags.WALL_HANGING_SIGNS).add(wallHangingSign);
            tag(BlockTags.LEAVES).add(leaves);

            tag(BlockTags.MINEABLE_WITH_AXE).add(log, wood, strippedLog, strippedWood, planks, stairs, slab,
                    fence, fenceGate, door, trapdoor, button, pressurePlate, sign, wallSign, hangingSign,
                    wallHangingSign, shelf);
            tag(BlockTags.MINEABLE_WITH_HOE).add(leaves);
            tag(BlockTags.SWORD_EFFICIENT).add(leaves);
        }
    }
}
