package team.xcmt.sam.datagen;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 由 tools/generate_datagen_templates.ps1 自动生成，请勿手工修改。
 * <p>
 * 木种共享的合成配方模板（内容与木种无关，仅 id 需要替换）。
 */
public final class WoodRecipeTemplates {

    private WoodRecipeTemplates() {
    }

    public static final String BOAT = """
{
  "type": "minecraft:crafting_shaped",
  "group": "boat",
  "key": {
    "#": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "# #",
    "###"
  ],
  "result": {
    "id": "%NS%:%WOOD%_boat"
  }
}
""";

    public static final String BUTTON = """
{
  "type": "minecraft:crafting_shapeless",
  "category": "redstone",
  "group": "wooden_button",
  "ingredients": [
    "%NS%:%WOOD%_planks"
  ],
  "result": {
    "id": "%NS%:%WOOD%_button"
  }
}
""";

    public static final String CHEST_BOAT = """
{
  "type": "minecraft:crafting_shapeless",
  "group": "chest_boat",
  "ingredients": [
    "minecraft:chest",
    "%NS%:%WOOD%_boat"
  ],
  "result": {
    "id": "%NS%:%WOOD%_chest_boat"
  }
}
""";

    public static final String DOOR = """
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "group": "wooden_door",
  "key": {
    "#": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "##",
    "##",
    "##"
  ],
  "result": {
    "count": 3,
    "id": "%NS%:%WOOD%_door"
  }
}
""";

    public static final String FENCE_GATE = """
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "group": "wooden_fence_gate",
  "key": {
    "#": "minecraft:stick",
    "W": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "#W#",
    "#W#"
  ],
  "result": {
    "id": "%NS%:%WOOD%_fence_gate"
  }
}
""";

    public static final String FENCE = """
{
  "type": "minecraft:crafting_shaped",
  "group": "wooden_fence",
  "key": {
    "#": "minecraft:stick",
    "W": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "W#W",
    "W#W"
  ],
  "result": {
    "count": 3,
    "id": "%NS%:%WOOD%_fence"
  }
}
""";

    public static final String HANGING_SIGN = """
{
  "type": "minecraft:crafting_shaped",
  "group": "wooden_hanging_sign",
  "key": {
    "#": "%NS%:stripped_%WOOD%_log",
    "X": "minecraft:iron_chain"
  },
  "pattern": [
    "X X",
    "###",
    "###"
  ],
  "result": {
    "count": 6,
    "id": "%NS%:%WOOD%_hanging_sign"
  }
}
""";

    public static final String PLANKS = """
{
  "type": "minecraft:crafting_shapeless",
  "category": "building",
  "group": "planks",
  "ingredients": [
    "#%NS%:%WOOD%_logs"
  ],
  "result": {
    "count": 4,
    "id": "%NS%:%WOOD%_planks"
  }
}
""";

    public static final String PRESSURE_PLATE = """
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "group": "wooden_pressure_plate",
  "key": {
    "#": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "##"
  ],
  "result": {
    "id": "%NS%:%WOOD%_pressure_plate"
  }
}
""";

    public static final String SHELF = """
{
  "type": "minecraft:crafting_shaped",
  "group": "shelf",
  "key": {
    "#": "%NS%:stripped_%WOOD%_log"
  },
  "pattern": [
    "###",
    "   ",
    "###"
  ],
  "result": {
    "count": 6,
    "id": "%NS%:%WOOD%_shelf"
  }
}
""";

    public static final String SIGN = """
{
  "type": "minecraft:crafting_shaped",
  "group": "wooden_sign",
  "key": {
    "#": "%NS%:%WOOD%_planks",
    "X": "minecraft:stick"
  },
  "pattern": [
    "###",
    "###",
    " X "
  ],
  "result": {
    "count": 3,
    "id": "%NS%:%WOOD%_sign"
  }
}
""";

    public static final String SLAB = """
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "group": "wooden_slab",
  "key": {
    "#": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "###"
  ],
  "result": {
    "count": 6,
    "id": "%NS%:%WOOD%_slab"
  }
}
""";

    public static final String STAIRS = """
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "group": "wooden_stairs",
  "key": {
    "#": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "#  ",
    "## ",
    "###"
  ],
  "result": {
    "count": 4,
    "id": "%NS%:%WOOD%_stairs"
  }
}
""";

    public static final String TRAPDOOR = """
{
  "type": "minecraft:crafting_shaped",
  "category": "redstone",
  "group": "wooden_trapdoor",
  "key": {
    "#": "%NS%:%WOOD%_planks"
  },
  "pattern": [
    "###",
    "###"
  ],
  "result": {
    "count": 2,
    "id": "%NS%:%WOOD%_trapdoor"
  }
}
""";

    public static final String WOOD = """
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "group": "bark",
  "key": {
    "#": "%NS%:%WOOD%_log"
  },
  "pattern": [
    "##",
    "##"
  ],
  "result": {
    "count": 3,
    "id": "%NS%:%WOOD%_wood"
  }
}
""";

    public static final String STRIPPED_WOOD = """
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "group": "bark",
  "key": {
    "#": "%NS%:stripped_%WOOD%_log"
  },
  "pattern": [
    "##",
    "##"
  ],
  "result": {
    "count": 3,
    "id": "%NS%:stripped_%WOOD%_wood"
  }
}
""";

    /** 名称模板（%s 为木种名）-> JSON 内容 */
    public static final Map<String, String> TEMPLATES;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("%s_boat", BOAT);
        map.put("%s_button", BUTTON);
        map.put("%s_chest_boat", CHEST_BOAT);
        map.put("%s_door", DOOR);
        map.put("%s_fence_gate", FENCE_GATE);
        map.put("%s_fence", FENCE);
        map.put("%s_hanging_sign", HANGING_SIGN);
        map.put("%s_planks", PLANKS);
        map.put("%s_pressure_plate", PRESSURE_PLATE);
        map.put("%s_shelf", SHELF);
        map.put("%s_sign", SIGN);
        map.put("%s_slab", SLAB);
        map.put("%s_stairs", STAIRS);
        map.put("%s_trapdoor", TRAPDOOR);
        map.put("%s_wood", WOOD);
        map.put("stripped_%s_wood", STRIPPED_WOOD);
        TEMPLATES = Collections.unmodifiableMap(map);
    }
}
