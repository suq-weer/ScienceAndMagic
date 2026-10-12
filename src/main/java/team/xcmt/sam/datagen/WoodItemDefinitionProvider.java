package team.xcmt.sam.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;

/**
 * 生成 MC 26.2 的「物品定义」文件 {@code assets/<ns>/items/<id>.json}。
 * <p>
 * 26.2 里 {@code models/item/*.json} 必须由物品定义文件指向才会生效。本方案不做任何
 * {@code tints} 染色——颜色已经在贴图里了，因此每个物品只是简单地指向本木种的模型：
 * <ul>
 *     <li>方块物品 → 直接指向本木种的方块模型（{@code <ns>:block/wood/<wood>/<model>}）</li>
 *     <li>非方块物品（门/告示牌/船/树苗等）→ 指向本木种生成的物品模型
 *         （形如 {@code <ns>:item/wood/<木种>/<模型键>}，由
 *         {@link team.xcmt.sam.datagen.WoodModelProvider} 产出）</li>
 * </ul>
 */
public final class WoodItemDefinitionProvider implements DataProvider {

    /**
     * @param itemName  物品名模板（{@code %s} 为木种名）
     * @param model     模型键；{@code @} 前缀表示这是自建物品模型而不是方块模型
     * @param boatOnly  是否仅在木种有船时生成
     */
    private record Entry(String itemName, String model, boolean boatOnly) {
        Entry(String itemName, String model) {
            this(itemName, model, false);
        }

        boolean generated() {
            return this.model.startsWith("@");
        }
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry("%s_log", "log"),
            new Entry("%s_wood", "wood"),
            new Entry("stripped_%s_log", "stripped_log"),
            new Entry("stripped_%s_wood", "stripped_wood"),
            new Entry("%s_planks", "planks"),
            new Entry("%s_stairs", "stairs"),
            new Entry("%s_slab", "slab"),
            new Entry("%s_fence", "fence_inventory"),
            new Entry("%s_fence_gate", "fence_gate"),
            new Entry("%s_trapdoor", "trapdoor_bottom"),
            new Entry("%s_button", "button_inventory"),
            new Entry("%s_pressure_plate", "pressure_plate"),
            new Entry("%s_shelf", "shelf_inventory"),
            new Entry("%s_leaves", "leaves"),
            // 以下物品的方块模型不适合做图标（或用原版惯例就是平面图标），使用自建物品模型。
            new Entry("%s_sapling", "@sapling"),
            new Entry("%s_door", "@door"),
            new Entry("%s_sign", "@sign"),
            new Entry("%s_hanging_sign", "@hanging_sign"),
            new Entry("%s_boat", "@boat", true),
            new Entry("%s_chest_boat", "@chest_boat", true));

    private final PackOutput.PathProvider pathProvider;

    public WoodItemDefinitionProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();
            for (Entry entry : ENTRIES) {
                if (entry.boatOnly() && !material.hasBoat()) {
                    continue;
                }
                String itemName = entry.itemName().replace("%s", material.name());
                String modelKey = entry.generated() ? entry.model().substring(1) : entry.model();
                Identifier model = entry.generated()
                        ? material.itemModel(modelKey)
                        : material.woodModel(modelKey);
                futures.add(DataProvider.saveStable(cache, buildJson(model.toString()),
                        this.pathProvider.json(Identifier.fromNamespaceAndPath(material.namespace(), itemName))));
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static JsonObject buildJson(String model) {
        JsonObject modelNode = new JsonObject();
        modelNode.addProperty("type", "minecraft:model");
        modelNode.addProperty("model", model);

        JsonObject root = new JsonObject();
        root.add("model", modelNode);
        return root;
    }

    @Override
    public @NonNull String getName() {
        return "Wood Item Definitions";
    }
}
