package team.xcmt.sam.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Rotation;

import team.xcmt.sam.api.wood.PlacerTreeShape;
import team.xcmt.sam.api.wood.TemplateTreeShape;
import team.xcmt.sam.api.wood.TreePreset;
import team.xcmt.sam.api.wood.TreeShape;
import team.xcmt.sam.api.wood.TreeShapes;

/**
 * 编译后的预设文件格式：{@code data/<namespace>/tree_preset/<预设>.json}。
 * <p>
 * 由 {@code runData}（{@code WoodTreePresetProvider}）写出，运行时由 {@code TreePresetCatalogLoader}
 * 读回。<b>读写放在同一个类里</b>是为了让格式不可能两头走样。
 * <pre>
 * {
 *   "format": 1,
 *   "placement_count": 5,
 *   "bucket_weights": { "3": 0.11 },
 *   "buckets": {
 *     "1": [ { "type": "builtin",  "shape": "pine", "weight": 4 } ],
 *     "3": [ { "type": "template",
 *              "template": "science_and_magic:tree_shapes/fancy/3x3/fancy_big_tree",
 *              "rotations": ["none"], "weight": 1 } ]
 *   }
 * }
 * </pre>
 * {@code type=builtin} 按 {@code shape} 在 {@link TreeShapes} 里查；
 * {@code type=template} 只写蓝图 id，形状 id 由 {@link TemplateTreeShape#fromTemplate} 反推
 * ——推导规则由 {@link TemplateTreeShape} 独家持有，读侧不做任何字符串裁剪，因此不会与 datagen 脱节。
 */
public final class TreePresetFile {

    /** 当前格式版本。解析到别的版本时直接报错，而不是猜。 */
    public static final int FORMAT = 1;

    private TreePresetFile() {
    }

    // ------------------------------------------------------------------
    // 写（datagen）
    // ------------------------------------------------------------------

    /** 把一个预设序列化成上面的 JSON。 */
    public static JsonObject write(TreePreset preset) {
        JsonObject root = new JsonObject();
        root.addProperty("format", FORMAT);
        root.addProperty("placement_count", preset.placementCount());

        if (!preset.bucketWeights().isEmpty()) {
            JsonObject weights = new JsonObject();
            preset.bucketWeights().forEach((size, weight) -> weights.addProperty(Integer.toString(size), weight));
            root.add("bucket_weights", weights);
        }

        JsonObject buckets = new JsonObject();
        for (Map.Entry<Integer, WeightedList<TreeShape>> entry : preset.buckets().entrySet()) {
            JsonArray members = new JsonArray();
            for (Weighted<TreeShape> weighted : entry.getValue().unwrap()) {
                JsonObject member = new JsonObject();
                switch (weighted.value()) {
                    case PlacerTreeShape placer -> {
                        member.addProperty("type", "builtin");
                        member.addProperty("shape", placer.name());
                    }
                    case TemplateTreeShape template -> {
                        member.addProperty("type", "template");
                        member.addProperty("template", template.template().toString());
                        JsonArray rotations = new JsonArray();
                        template.rotations().forEach(rotation -> rotations.add(rotation.getSerializedName()));
                        member.add("rotations", rotations);
                    }
                    default -> throw new IllegalStateException(
                            "无法序列化的形状类型：" + weighted.value().getClass().getName());
                }
                member.addProperty("weight", weighted.weight());
                members.add(member);
            }
            buckets.add(Integer.toString(entry.getKey()), members);
        }
        root.add("buckets", buckets);
        return root;
    }

    // ------------------------------------------------------------------
    // 读（运行时）
    // ------------------------------------------------------------------

    /**
     * 解析一个预设文件。
     *
     * @param presetId 由文件名推出的预设 id
     * @throws IllegalArgumentException 格式不合法（消息面向数据包作者）
     */
    public static TreePreset parse(Identifier presetId, JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            throw new IllegalArgumentException("必须是一个 JSON 对象");
        }
        JsonObject json = root.getAsJsonObject();

        if (!json.has("format")) {
            throw new IllegalArgumentException("缺少 format 字段（当前版本 " + FORMAT + "）");
        }
        int format = json.get("format").getAsInt();
        if (format != FORMAT) {
            throw new IllegalArgumentException("format 版本不支持：期望 " + FORMAT + "，收到 " + format);
        }

        TreePreset.Builder builder = TreePreset.builder(presetId);
        if (json.has("placement_count")) {
            builder.placementCount(json.get("placement_count").getAsInt());
        }

        if (json.has("bucket_weights")) {
            json.getAsJsonObject("bucket_weights").entrySet().forEach(entry ->
                    builder.bucketWeight(Integer.parseInt(entry.getKey()), entry.getValue().getAsDouble()));
        }

        if (!json.has("buckets") || !json.get("buckets").isJsonObject()) {
            throw new IllegalArgumentException("缺少 buckets 对象");
        }
        for (Map.Entry<String, JsonElement> bucket : json.getAsJsonObject("buckets").entrySet()) {
            int size = Integer.parseInt(bucket.getKey());
            if (!bucket.getValue().isJsonArray()) {
                throw new IllegalArgumentException("buckets[" + bucket.getKey() + "] 必须是数组");
            }
            for (JsonElement element : bucket.getValue().getAsJsonArray()) {
                Weighted<TreeShape> member = parseMember(size, element);
                builder.add(size, member.value(), member.weight());
            }
        }
        return builder.build();
    }

    private static Weighted<TreeShape> parseMember(int size, JsonElement element) {
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("桶 " + size + "x" + size + " 的成员必须是对象");
        }
        JsonObject member = element.getAsJsonObject();
        int weight = member.has("weight") ? member.get("weight").getAsInt() : 1;
        String type = member.has("type") ? member.get("type").getAsString() : "";

        return switch (type) {
            case "builtin" -> {
                String path = member.get("shape").getAsString();
                PlacerTreeShape shape = TreeShapes.byPath(path);
                if (shape == null) {
                    throw new IllegalArgumentException("未知的内置形状 " + path + "；可用：" + String.join(", ", TreeShapes.paths()));
                }
                yield new Weighted<>(shape, weight);
            }
            case "template" -> {
                Identifier template = Identifier.parse(member.get("template").getAsString());
                yield new Weighted<>(TemplateTreeShape.fromTemplate(template, size, rotationsOf(member)), weight);
            }
            default -> throw new IllegalArgumentException(
                    "成员缺少 type（只支持 builtin / template），收到：" + (type.isEmpty() ? "<缺失>" : type));
        };
    }

    private static List<Rotation> rotationsOf(JsonObject member) {
        if (!member.has("rotations") || !member.get("rotations").isJsonArray()) {
            return List.of(Rotation.NONE);
        }
        List<Rotation> rotations = new ArrayList<>();
        for (JsonElement element : member.getAsJsonArray("rotations")) {
            String name = element.getAsString();
            Optional<Rotation> found = java.util.Arrays.stream(Rotation.values())
                    .filter(rotation -> rotation.getSerializedName().equals(name))
                    .findFirst();
            if (found.isEmpty()) {
                throw new IllegalArgumentException("未知的旋转 " + name + "（可用：" + rotationNames() + "）");
            }
            rotations.add(found.get());
        }
        return rotations.isEmpty() ? List.of(Rotation.NONE) : List.copyOf(rotations);
    }

    private static String rotationNames() {
        return String.join(", ", java.util.Arrays.stream(Rotation.values()).map(Rotation::getSerializedName).toList());
    }
}
