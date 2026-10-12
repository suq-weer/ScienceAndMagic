package team.xcmt.sam.datagen;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;

/**
 * 物品标签：加入原版物品标签，并生成木种自己的 {@code <ns>:<wood>_logs} 物品标签
 * （木板配方 {@code #<ns>:<wood>_logs} 依赖它）。
 */
public final class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            String modId) {
        super(output, lookupProvider, modId);
    }

    // IntrinsicTagAppender#add(T...) 对泛型元素会创建泛型数组，javac 会报不经检查的转换；
    // 这里传入的始终是 ResourceKey<Item>，转换是安全的。
    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            TagKey<Item> logs = TagKey.create(Registries.ITEM, material.id("_logs"));

            ResourceKey<Item> log = key(set, "log");
            ResourceKey<Item> wood = key(set, "wood");
            ResourceKey<Item> strippedLog = key(set, "stripped_log");
            ResourceKey<Item> strippedWood = key(set, "stripped_wood");

            tag(ItemTags.LOGS).add(log, wood, strippedLog, strippedWood);
            tag(ItemTags.LOGS_THAT_BURN).add(log, wood, strippedLog, strippedWood);
            tag(logs).add(log, wood, strippedLog, strippedWood);

            tag(Tags.Items.STRIPPED_LOGS).add(strippedLog);
            tag(Tags.Items.STRIPPED_WOODS).add(strippedWood);

            tag(ItemTags.PLANKS).add(key(set, "planks"));
            tag(ItemTags.WOODEN_STAIRS).add(key(set, "stairs"));
            tag(ItemTags.WOODEN_SLABS).add(key(set, "slab"));
            tag(ItemTags.WOODEN_FENCES).add(key(set, "fence"));
            tag(ItemTags.FENCE_GATES).add(key(set, "fence_gate"));
            tag(ItemTags.WOODEN_DOORS).add(key(set, "door"));
            tag(ItemTags.WOODEN_TRAPDOORS).add(key(set, "trapdoor"));
            tag(ItemTags.WOODEN_BUTTONS).add(key(set, "button"));
            tag(ItemTags.WOODEN_PRESSURE_PLATES).add(key(set, "pressure_plate"));
            tag(ItemTags.SIGNS).add(key(set, "sign"));
            tag(ItemTags.HANGING_SIGNS).add(key(set, "hanging_sign"));
            tag(ItemTags.LEAVES).add(key(set, "leaves"));
            tag(ItemTags.SAPLINGS).add(key(set, "sapling"));

            if (material.hasBoat()) {
                tag(ItemTags.BOATS).add(key(set, "boat"));
                tag(ItemTags.CHEST_BOATS).add(key(set, "chest_boat"));
            }
        }
    }

    /**
     * 取木种某后缀物品的注册键。
     * <p>
     * {@link WoodSet#itemKey(String)} 对未知后缀返回 {@code null}；这里传入的都是框架保证会注册的后缀，
     * 出现 {@code null} 说明木种定义不完整，直接失败比生成一份缺项的标签更好排查。
     */
    private static ResourceKey<Item> key(WoodSet set, String suffix) {
        return Objects.requireNonNull(set.itemKey(suffix),
                () -> "木种 " + set.material().name() + " 缺少物品后缀 " + suffix);
    }
}
