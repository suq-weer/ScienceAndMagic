package team.xcmt.sam.datagen;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.data.PackOutput;

/**
 * 生成方块战利品表（模板见 {@link WoodLootTemplates}）。
 * <p>
 * 原版没有为墙上的告示牌单独提供战利品表，这里额外补两份，保证拆掉墙告示牌时也会掉落对应物品。
 */
public final class WoodLootProvider extends TemplatedJsonProvider {

    /** 墙告示牌战利品表模板；{@code %NS%} / {@code %WOOD%} 由 {@link #expand} 展开。 */
    private static final String WALL_SIGN_LOOT = """
            {
              "type": "minecraft:block",
              "pools": [
                {
                  "conditions": [
                    {
                      "condition": "minecraft:survives_explosion"
                    }
                  ],
                  "entries": [
                    {
                      "type": "minecraft:item",
                      "name": "%NS%:%WOOD%_sign"
                    }
                  ],
                  "rolls": 1.0
                }
              ],
              "random_sequence": "%NS%:blocks/%WOOD%_wall_sign"
            }
            """;

    /** 墙悬挂告示牌战利品表模板。 */
    private static final String WALL_HANGING_SIGN_LOOT = """
            {
              "type": "minecraft:block",
              "pools": [
                {
                  "conditions": [
                    {
                      "condition": "minecraft:survives_explosion"
                    }
                  ],
                  "entries": [
                    {
                      "type": "minecraft:item",
                      "name": "%NS%:%WOOD%_hanging_sign"
                    }
                  ],
                  "rolls": 1.0
                }
              ],
              "random_sequence": "%NS%:blocks/%WOOD%_wall_hanging_sign"
            }
            """;

    public WoodLootProvider(PackOutput output) {
        super(output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_table/blocks"),
                templates(), "Wood Loot Tables");
    }

    private static Map<String, String> templates() {
        Map<String, String> map = new LinkedHashMap<>(WoodLootTemplates.TEMPLATES);
        map.put("%s_wall_sign", WALL_SIGN_LOOT);
        map.put("%s_wall_hanging_sign", WALL_HANGING_SIGN_LOOT);
        return map;
    }
}
