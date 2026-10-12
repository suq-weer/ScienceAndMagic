package team.xcmt.sam.datagen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.api.wood.WoodMaterial;
import team.xcmt.sam.api.wood.WoodSet;
import team.xcmt.sam.api.wood.WoodTexture;

/**
 * 生成每个木种的方块模型与「非方块物品」的物品模型。
 * <p>
 * <b>方块模型</b>（{@code assets/<ns>/models/block/wood/<木种>/<键>.json}）都是薄模型：
 * {@code parent} 指向<b>原版几何模板</b>（{@code minecraft:block/stairs} 等），
 * {@code textures} 指向本木种重着色后的 sprite。因为不再需要 {@code tintindex}，
 * 就不必再复制并改写原版模型，一个木种一套薄模型即可。
 * <p>
 * <b>物品模型</b>（{@code assets/<ns>/models/item/wood/<木种>/<键>.json}）只用于那些
 * 「原版也提供平面图标」的物品（门 / 告示牌 / 悬挂告示牌 / 船 / 树苗）；
 * 其余物品在 {@link WoodItemDefinitionProvider} 里直接引用方块模型，与原版做法一致。
 * <p>
 * <b>树叶</b>刻意使用 {@code minecraft:block/cube_all} 而不是 {@code minecraft:block/leaves}：
 * 后者的模型面带 {@code tintindex:0}，会再叠一层生物群系叶色，把已经染好色的贴图二次着色。
 */
public final class WoodModelProvider implements DataProvider {

    /** 方块模型：模型键、原版几何模板 parent、贴图槽位 → 贴图类别。 */
    private record BlockModel(String key, String parent, Map<String, WoodTexture> textures) {
    }

    /** 自建物品模型：模型键、贴图类别。parent 与贴图槽位固定，见下方两个常量。 */
    private record ItemModel(String key, WoodTexture texture) {
    }

    private static final String CUBE_ALL = "minecraft:block/cube_all";
    private static final String CUBE_COLUMN = "minecraft:block/cube_column";
    private static final String CUBE_COLUMN_HORIZONTAL = "minecraft:block/cube_column_horizontal";

    /** 自建物品模型统一挂在原版的生成式物品模型上。 */
    private static final String ITEM_MODEL_PARENT = "minecraft:item/generated";
    /** 自建物品模型统一使用的贴图槽位。 */
    private static final String ITEM_MODEL_SLOT = "layer0";

    private static final List<BlockModel> BLOCK_MODELS = buildBlockModels();

    private static final List<ItemModel> ITEM_MODELS = List.of(
            // 树苗原版就是「平面图标 + 方块贴图」，沿用同样的结构。
            new ItemModel("sapling", WoodTexture.SAPLING),
            new ItemModel("door", WoodTexture.ITEM_DOOR),
            new ItemModel("sign", WoodTexture.ITEM_SIGN),
            new ItemModel("hanging_sign", WoodTexture.ITEM_HANGING_SIGN),
            new ItemModel("boat", WoodTexture.ITEM_BOAT),
            new ItemModel("chest_boat", WoodTexture.ITEM_CHEST_BOAT));

    /** 只在 {@link WoodMaterial#hasBoat()} 为真时才生成的自建物品模型。 */
    private static final List<String> BOAT_ITEM_MODELS = List.of("boat", "chest_boat");

    private final PackOutput.PathProvider blockPathProvider;
    private final PackOutput.PathProvider itemPathProvider;

    public WoodModelProvider(PackOutput output) {
        this.blockPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.itemPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (WoodSet set : WoodSet.all()) {
            WoodMaterial material = set.material();

            for (BlockModel model : BLOCK_MODELS) {
                futures.add(DataProvider.saveStable(cache, blockJson(material, model),
                        this.blockPathProvider.json(blockFilePath(material, model.key()))));
            }

            for (ItemModel model : ITEM_MODELS) {
                if (BOAT_ITEM_MODELS.contains(model.key()) && !material.hasBoat()) {
                    continue;
                }
                futures.add(DataProvider.saveStable(cache, itemJson(material, model),
                        this.itemPathProvider.json(itemFilePath(material, model.key()))));
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /** 方块模型文件 ID：{@code <ns>:wood/<木种>/<键>} → {@code models/block/wood/<木种>/<键>.json}。 */
    private static Identifier blockFilePath(WoodMaterial material, String key) {
        return Identifier.fromNamespaceAndPath(material.namespace(), "wood/" + material.name() + "/" + key);
    }

    /** 物品模型文件 ID：{@code <ns>:wood/<木种>/<键>} → {@code models/item/wood/<木种>/<键>.json}。 */
    private static Identifier itemFilePath(WoodMaterial material, String key) {
        return Identifier.fromNamespaceAndPath(material.namespace(), "wood/" + material.name() + "/" + key);
    }

    private static JsonObject blockJson(WoodMaterial material, BlockModel model) {
        Map<String, String> textures = new LinkedHashMap<>();
        model.textures().forEach((slot, texture) -> textures.put(slot, material.sprite(texture).toString()));
        return modelJson(model.parent(), textures);
    }

    private static JsonObject itemJson(WoodMaterial material, ItemModel model) {
        Map<String, String> textures = new LinkedHashMap<>();
        textures.put(ITEM_MODEL_SLOT, material.sprite(model.texture()).toString());
        return modelJson(ITEM_MODEL_PARENT, textures);
    }

    private static JsonObject modelJson(String parent, Map<String, String> textures) {
        JsonObject textureNode = new JsonObject();
        textures.forEach(textureNode::addProperty);

        JsonObject root = new JsonObject();
        root.addProperty("parent", parent);
        root.add("textures", textureNode);
        return root;
    }

    // ------------------------------------------------------------------
    // 模型表
    // ------------------------------------------------------------------

    private static List<BlockModel> buildBlockModels() {
        List<BlockModel> models = new ArrayList<>();

        // 基础方块
        models.add(new BlockModel("planks", CUBE_ALL, Map.of("all", WoodTexture.PLANKS)));
        models.add(new BlockModel("log", CUBE_COLUMN,
                Map.of("end", WoodTexture.LOG_TOP, "side", WoodTexture.LOG)));
        models.add(new BlockModel("log_horizontal", CUBE_COLUMN_HORIZONTAL,
                Map.of("end", WoodTexture.LOG_TOP, "side", WoodTexture.LOG)));
        models.add(new BlockModel("wood", CUBE_COLUMN,
                Map.of("end", WoodTexture.LOG, "side", WoodTexture.LOG)));
        models.add(new BlockModel("stripped_log", CUBE_COLUMN,
                Map.of("end", WoodTexture.STRIPPED_LOG_TOP, "side", WoodTexture.STRIPPED_LOG)));
        models.add(new BlockModel("stripped_log_horizontal", CUBE_COLUMN_HORIZONTAL,
                Map.of("end", WoodTexture.STRIPPED_LOG_TOP, "side", WoodTexture.STRIPPED_LOG)));
        models.add(new BlockModel("stripped_wood", CUBE_COLUMN,
                Map.of("end", WoodTexture.STRIPPED_LOG, "side", WoodTexture.STRIPPED_LOG)));

        // 由木板派生的方块（与原版一致，全部复用木板贴图）
        models.add(planksSlots("stairs", "minecraft:block/stairs"));
        models.add(planksSlots("stairs_inner", "minecraft:block/inner_stairs"));
        models.add(planksSlots("stairs_outer", "minecraft:block/outer_stairs"));
        models.add(planksSlots("slab", "minecraft:block/slab"));
        models.add(planksSlots("slab_top", "minecraft:block/slab_top"));
        models.add(singleSlot("fence_post", "minecraft:block/fence_post", WoodTexture.PLANKS));
        models.add(singleSlot("fence_side", "minecraft:block/fence_side", WoodTexture.PLANKS));
        models.add(singleSlot("fence_inventory", "minecraft:block/fence_inventory", WoodTexture.PLANKS));
        models.add(singleSlot("fence_gate", "minecraft:block/template_fence_gate", WoodTexture.PLANKS));
        models.add(singleSlot("fence_gate_open", "minecraft:block/template_fence_gate_open", WoodTexture.PLANKS));
        models.add(singleSlot("fence_gate_wall", "minecraft:block/template_fence_gate_wall", WoodTexture.PLANKS));
        models.add(singleSlot("fence_gate_wall_open", "minecraft:block/template_fence_gate_wall_open",
                WoodTexture.PLANKS));
        models.add(singleSlot("button", "minecraft:block/button", WoodTexture.PLANKS));
        models.add(singleSlot("button_pressed", "minecraft:block/button_pressed", WoodTexture.PLANKS));
        models.add(singleSlot("button_inventory", "minecraft:block/button_inventory", WoodTexture.PLANKS));
        models.add(singleSlot("pressure_plate", "minecraft:block/pressure_plate_up", WoodTexture.PLANKS));
        models.add(singleSlot("pressure_plate_down", "minecraft:block/pressure_plate_down", WoodTexture.PLANKS));

        // 门（上下两段各有独立贴图）与活板门
        models.add(doorSlots("door_bottom_left", "minecraft:block/door_bottom_left"));
        models.add(doorSlots("door_bottom_left_open", "minecraft:block/door_bottom_left_open"));
        models.add(doorSlots("door_bottom_right", "minecraft:block/door_bottom_right"));
        models.add(doorSlots("door_bottom_right_open", "minecraft:block/door_bottom_right_open"));
        models.add(doorSlots("door_top_left", "minecraft:block/door_top_left"));
        models.add(doorSlots("door_top_left_open", "minecraft:block/door_top_left_open"));
        models.add(doorSlots("door_top_right", "minecraft:block/door_top_right"));
        models.add(doorSlots("door_top_right_open", "minecraft:block/door_top_right_open"));
        models.add(singleSlot("trapdoor_bottom", "minecraft:block/template_trapdoor_bottom",
                WoodTexture.TRAPDOOR));
        models.add(singleSlot("trapdoor_open", "minecraft:block/template_trapdoor_open", WoodTexture.TRAPDOOR));
        models.add(singleSlot("trapdoor_top", "minecraft:block/template_trapdoor_top", WoodTexture.TRAPDOOR));

        // 告示牌族：粒子贴图沿用木板/去皮原木，与原版一致
        for (int rotation = 0; rotation < 4; rotation++) {
            models.add(signSlots("sign_rot_" + rotation, "minecraft:block/template_sign_rot_" + rotation));
            models.add(hangingSignSlots("hanging_sign_rot_" + rotation,
                    "minecraft:block/template_hanging_sign_rot_" + rotation));
            models.add(hangingSignSlots("hanging_sign_attached_rot_" + rotation,
                    "minecraft:block/template_attached_hanging_sign_rot_" + rotation));
        }
        models.add(signSlots("wall_sign", "minecraft:block/template_wall_sign"));
        models.add(hangingSignSlots("wall_hanging_sign", "minecraft:block/template_wall_hanging_sign"));

        // 货架（26.2 新增）
        models.add(shelfSlots("shelf", "minecraft:block/template_shelf_body"));
        models.add(shelfSlots("shelf_center", "minecraft:block/template_shelf_center"));
        models.add(shelfSlots("shelf_left", "minecraft:block/template_shelf_left"));
        models.add(shelfSlots("shelf_right", "minecraft:block/template_shelf_right"));
        models.add(shelfSlots("shelf_unconnected", "minecraft:block/template_shelf_unconnected"));
        models.add(shelfSlots("shelf_unpowered", "minecraft:block/template_shelf_unpowered"));
        models.add(shelfSlots("shelf_inventory", "minecraft:block/template_shelf_inventory"));

        // 树叶与树苗
        models.add(new BlockModel("leaves", CUBE_ALL, Map.of("all", WoodTexture.LEAVES)));
        models.add(new BlockModel("sapling", "minecraft:block/cross", Map.of("cross", WoodTexture.SAPLING)));

        return List.copyOf(models);
    }

    /** bottom/top/side 三槽全用木板贴图（楼梯、台阶）。 */
    private static BlockModel planksSlots(String key, String parent) {
        return new BlockModel(key, parent, Map.of(
                "bottom", WoodTexture.PLANKS,
                "top", WoodTexture.PLANKS,
                "side", WoodTexture.PLANKS));
    }

    /** 单槽 {@code texture}（栅栏、栅栏门、按钮、压力板、活板门）。 */
    private static BlockModel singleSlot(String key, String parent, WoodTexture texture) {
        return new BlockModel(key, parent, Map.of("texture", texture));
    }

    /** 门的 bottom/top 两槽。 */
    private static BlockModel doorSlots(String key, String parent) {
        return new BlockModel(key, parent, Map.of(
                "bottom", WoodTexture.DOOR_BOTTOM,
                "top", WoodTexture.DOOR_TOP));
    }

    /** 告示牌：牌面 + 粒子（木板）。 */
    private static BlockModel signSlots(String key, String parent) {
        return new BlockModel(key, parent, Map.of(
                "all", WoodTexture.SIGN,
                "particle", WoodTexture.PLANKS));
    }

    /** 悬挂告示牌：牌面 + 粒子（去皮原木）。 */
    private static BlockModel hangingSignSlots(String key, String parent) {
        return new BlockModel(key, parent, Map.of(
                "all", WoodTexture.HANGING_SIGN,
                "particle", WoodTexture.STRIPPED_LOG));
    }

    /** 货架：架体 + 粒子（去皮原木）。 */
    private static BlockModel shelfSlots(String key, String parent) {
        return new BlockModel(key, parent, Map.of(
                "all", WoodTexture.SHELF,
                "particle", WoodTexture.STRIPPED_LOG));
    }

    @Override
    public @NonNull String getName() {
        return "Wood Models";
    }
}
