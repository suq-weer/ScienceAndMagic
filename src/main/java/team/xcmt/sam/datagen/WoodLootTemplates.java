// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 由 tools/generate_datagen_templates.ps1 自动生成，请勿手工修改。
 * <p>
 * 木种共享的方块战利品表模板（内容与木种无关，仅 id 需要替换）。
 */
public final class WoodLootTemplates {

    private WoodLootTemplates() {
    }

    public static final String BUTTON = """
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
          "name": "%NS%:%WOOD%_button"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_button"
}
""";

    public static final String DOOR = """
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
          "conditions": [
            {
              "block": "%NS%:%WOOD%_door",
              "condition": "minecraft:block_state_property",
              "properties": {
                "half": "lower"
              }
            }
          ],
          "name": "%NS%:%WOOD%_door"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_door"
}
""";

    public static final String FENCE_GATE = """
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
          "name": "%NS%:%WOOD%_fence_gate"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_fence_gate"
}
""";

    public static final String FENCE = """
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
          "name": "%NS%:%WOOD%_fence"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_fence"
}
""";

    public static final String HANGING_SIGN = """
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
  "random_sequence": "%NS%:blocks/%WOOD%_hanging_sign"
}
""";

    public static final String LEAVES = """
{
  "type": "minecraft:block",
  "pools": [
    {
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:any_of",
                  "terms": [
                    {
                      "condition": "minecraft:match_tool",
                      "predicate": {
                        "items": "minecraft:shears"
                      }
                    },
                    {
                      "condition": "minecraft:match_tool",
                      "predicate": {
                        "predicates": {
                          "minecraft:enchantments": [
                            {
                              "enchantments": "minecraft:silk_touch",
                              "levels": {
                                "min": 1
                              }
                            }
                          ]
                        }
                      }
                    }
                  ]
                }
              ],
              "name": "%NS%:%WOOD%_leaves"
            },
            {
              "type": "minecraft:item",
              "conditions": [
                {
                  "condition": "minecraft:survives_explosion"
                },
                {
                  "chances": [
                    0.05,
                    0.0625,
                    0.083333336,
                    0.1
                  ],
                  "condition": "minecraft:table_bonus",
                  "enchantment": "minecraft:fortune"
                }
              ],
              "name": "%NS%:%WOOD%_sapling"
            }
          ]
        }
      ],
      "rolls": 1.0
    },
    {
      "conditions": [
        {
          "condition": "minecraft:inverted",
          "term": {
            "condition": "minecraft:any_of",
            "terms": [
              {
                "condition": "minecraft:match_tool",
                "predicate": {
                  "items": "minecraft:shears"
                }
              },
              {
                "condition": "minecraft:match_tool",
                "predicate": {
                  "predicates": {
                    "minecraft:enchantments": [
                      {
                        "enchantments": "minecraft:silk_touch",
                        "levels": {
                          "min": 1
                        }
                      }
                    ]
                  }
                }
              }
            ]
          }
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "conditions": [
            {
              "chances": [
                0.02,
                0.022222223,
                0.025,
                0.033333335,
                0.1
              ],
              "condition": "minecraft:table_bonus",
              "enchantment": "minecraft:fortune"
            }
          ],
          "functions": [
            {
              "count": {
                "type": "minecraft:uniform",
                "max": 2.0,
                "min": 1.0
              },
              "function": "minecraft:set_count"
            },
            {
              "function": "minecraft:explosion_decay"
            }
          ],
          "name": "minecraft:stick"
        }
      ],
      "rolls": 1.0
    },
    {
      "conditions": [
        {
          "condition": "minecraft:inverted",
          "term": {
            "condition": "minecraft:any_of",
            "terms": [
              {
                "condition": "minecraft:match_tool",
                "predicate": {
                  "items": "minecraft:shears"
                }
              },
              {
                "condition": "minecraft:match_tool",
                "predicate": {
                  "predicates": {
                    "minecraft:enchantments": [
                      {
                        "enchantments": "minecraft:silk_touch",
                        "levels": {
                          "min": 1
                        }
                      }
                    ]
                  }
                }
              }
            ]
          }
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "conditions": [
            {
              "condition": "minecraft:survives_explosion"
            },
            {
              "chances": [
                0.005,
                0.0055555557,
                0.00625,
                0.008333334,
                0.025
              ],
              "condition": "minecraft:table_bonus",
              "enchantment": "minecraft:fortune"
            }
          ],
          "name": "minecraft:apple"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_leaves"
}
""";

    public static final String LOG = """
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
          "name": "%NS%:%WOOD%_log"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_log"
}
""";

    public static final String PLANKS = """
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
          "name": "%NS%:%WOOD%_planks"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_planks"
}
""";

    public static final String PRESSURE_PLATE = """
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
          "name": "%NS%:%WOOD%_pressure_plate"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_pressure_plate"
}
""";

    public static final String SAPLING = """
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
          "name": "%NS%:%WOOD%_sapling"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_sapling"
}
""";

    public static final String SHELF = """
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
          "name": "%NS%:%WOOD%_shelf"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_shelf"
}
""";

    public static final String SIGN = """
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
  "random_sequence": "%NS%:blocks/%WOOD%_sign"
}
""";

    public static final String SLAB = """
{
  "type": "minecraft:block",
  "pools": [
    {
      "entries": [
        {
          "type": "minecraft:item",
          "functions": [
            {
              "conditions": [
                {
                  "block": "%NS%:%WOOD%_slab",
                  "condition": "minecraft:block_state_property",
                  "properties": {
                    "type": "double"
                  }
                }
              ],
              "count": 2.0,
              "function": "minecraft:set_count"
            },
            {
              "function": "minecraft:explosion_decay"
            }
          ],
          "name": "%NS%:%WOOD%_slab"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_slab"
}
""";

    public static final String STAIRS = """
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
          "name": "%NS%:%WOOD%_stairs"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_stairs"
}
""";

    public static final String TRAPDOOR = """
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
          "name": "%NS%:%WOOD%_trapdoor"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_trapdoor"
}
""";

    public static final String WOOD = """
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
          "name": "%NS%:%WOOD%_wood"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "%NS%:blocks/%WOOD%_wood"
}
""";

    public static final String STRIPPED_LOG = """
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
          "name": "%NS%:stripped_%WOOD%_log"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "minecraft:blocks/stripped_oak_log"
}
""";

    public static final String STRIPPED_WOOD = """
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
          "name": "%NS%:stripped_%WOOD%_wood"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "minecraft:blocks/stripped_oak_wood"
}
""";

    /** 名称模板（%s 为木种名）-> JSON 内容 */
    public static final Map<String, String> TEMPLATES;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("%s_button", BUTTON);
        map.put("%s_door", DOOR);
        map.put("%s_fence_gate", FENCE_GATE);
        map.put("%s_fence", FENCE);
        map.put("%s_hanging_sign", HANGING_SIGN);
        map.put("%s_leaves", LEAVES);
        map.put("%s_log", LOG);
        map.put("%s_planks", PLANKS);
        map.put("%s_pressure_plate", PRESSURE_PLATE);
        map.put("%s_sapling", SAPLING);
        map.put("%s_shelf", SHELF);
        map.put("%s_sign", SIGN);
        map.put("%s_slab", SLAB);
        map.put("%s_stairs", STAIRS);
        map.put("%s_trapdoor", TRAPDOOR);
        map.put("%s_wood", WOOD);
        map.put("stripped_%s_log", STRIPPED_LOG);
        map.put("stripped_%s_wood", STRIPPED_WOOD);
        TEMPLATES = Collections.unmodifiableMap(map);
    }
}
