package team.xcmt.sam.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import team.xcmt.sam.ScienceAndMagic;
import team.xcmt.sam.api.wood.WoodSet;

/**
 * 模组创造模式物品栏。所有 {@code inCreativeTab} 为 {@code true} 的木种物品都会自动加入。
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ScienceAndMagic.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SCIENCE_AND_MAGIC =
            TABS.register("science_and_magic", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.science_and_magic.science_and_magic"))
                    .icon(() -> new ItemStack(ModBlocks.SEQUOIA_SET.sapling().get()))
                    .displayItems((parameters, output) -> {
                        for (WoodSet set : WoodSet.all()) {
                            if (!set.material().inCreativeTab()) {
                                continue;
                            }
                            for (Item item : set.items()) {
                                output.accept(item);
                            }
                        }
                        // 魂骨与测试装备
                        CuriosSoulBones.all().forEach(item -> output.accept(item.get()));
                        TestItems.all().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
