// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 XCM Team (Xiaosu <xiaosusn8@qq.com>)

package team.xcmt.sam.datagen;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 由 tools/generate_blockstate_templates.ps1 自动生成，请勿手工修改。
 * <p>
 * 木种的 blockstate 模板：键为方块名模板（{@code %s} 代表木种名），值为 blockstate JSON 内容。
 * 模型路径写成 {@code %NS%:block/wood/%WOOD%/<model>}，由 {@link TemplatedJsonProvider} 展开。
 */
public final class WoodBlockStateTemplates {

    private WoodBlockStateTemplates() {
    }

    public static final String BUTTON = """
{
  "variants": {
    "face=ceiling,facing=east,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "x": 180,
      "y": 270
    },
    "face=ceiling,facing=east,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "x": 180,
      "y": 270
    },
    "face=ceiling,facing=north,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "x": 180,
      "y": 180
    },
    "face=ceiling,facing=north,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "x": 180,
      "y": 180
    },
    "face=ceiling,facing=south,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "x": 180
    },
    "face=ceiling,facing=south,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "x": 180
    },
    "face=ceiling,facing=west,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "x": 180,
      "y": 90
    },
    "face=ceiling,facing=west,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "x": 180,
      "y": 90
    },
    "face=floor,facing=east,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "y": 90
    },
    "face=floor,facing=east,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "y": 90
    },
    "face=floor,facing=north,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button"
    },
    "face=floor,facing=north,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed"
    },
    "face=floor,facing=south,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "y": 180
    },
    "face=floor,facing=south,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "y": 180
    },
    "face=floor,facing=west,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "y": 270
    },
    "face=floor,facing=west,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "y": 270
    },
    "face=wall,facing=east,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "uvlock": true,
      "x": 90,
      "y": 90
    },
    "face=wall,facing=east,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "uvlock": true,
      "x": 90,
      "y": 90
    },
    "face=wall,facing=north,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "uvlock": true,
      "x": 90
    },
    "face=wall,facing=north,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "uvlock": true,
      "x": 90
    },
    "face=wall,facing=south,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "uvlock": true,
      "x": 90,
      "y": 180
    },
    "face=wall,facing=south,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "uvlock": true,
      "x": 90,
      "y": 180
    },
    "face=wall,facing=west,powered=false": {
      "model": "%NS%:block/wood/%WOOD%/button",
      "uvlock": true,
      "x": 90,
      "y": 270
    },
    "face=wall,facing=west,powered=true": {
      "model": "%NS%:block/wood/%WOOD%/button_pressed",
      "uvlock": true,
      "x": 90,
      "y": 270
    }
  }
}
""";

    public static final String DOOR = """
{
  "variants": {
    "facing=east,half=lower,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left"
    },
    "facing=east,half=lower,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left_open",
      "y": 90
    },
    "facing=east,half=lower,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right"
    },
    "facing=east,half=lower,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right_open",
      "y": 270
    },
    "facing=east,half=upper,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left"
    },
    "facing=east,half=upper,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left_open",
      "y": 90
    },
    "facing=east,half=upper,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right"
    },
    "facing=east,half=upper,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right_open",
      "y": 270
    },
    "facing=north,half=lower,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left",
      "y": 270
    },
    "facing=north,half=lower,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left_open"
    },
    "facing=north,half=lower,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right",
      "y": 270
    },
    "facing=north,half=lower,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right_open",
      "y": 180
    },
    "facing=north,half=upper,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left",
      "y": 270
    },
    "facing=north,half=upper,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left_open"
    },
    "facing=north,half=upper,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right",
      "y": 270
    },
    "facing=north,half=upper,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right_open",
      "y": 180
    },
    "facing=south,half=lower,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left",
      "y": 90
    },
    "facing=south,half=lower,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left_open",
      "y": 180
    },
    "facing=south,half=lower,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right",
      "y": 90
    },
    "facing=south,half=lower,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right_open"
    },
    "facing=south,half=upper,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left",
      "y": 90
    },
    "facing=south,half=upper,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left_open",
      "y": 180
    },
    "facing=south,half=upper,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right",
      "y": 90
    },
    "facing=south,half=upper,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right_open"
    },
    "facing=west,half=lower,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left",
      "y": 180
    },
    "facing=west,half=lower,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_left_open",
      "y": 270
    },
    "facing=west,half=lower,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right",
      "y": 180
    },
    "facing=west,half=lower,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_bottom_right_open",
      "y": 90
    },
    "facing=west,half=upper,hinge=left,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left",
      "y": 180
    },
    "facing=west,half=upper,hinge=left,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_left_open",
      "y": 270
    },
    "facing=west,half=upper,hinge=right,open=false": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right",
      "y": 180
    },
    "facing=west,half=upper,hinge=right,open=true": {
      "model": "%NS%:block/wood/%WOOD%/door_top_right_open",
      "y": 90
    }
  }
}
""";

    public static final String FENCE_GATE = """
{
  "variants": {
    "facing=east,in_wall=false,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate",
      "uvlock": true,
      "y": 270
    },
    "facing=east,in_wall=false,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_open",
      "uvlock": true,
      "y": 270
    },
    "facing=east,in_wall=true,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall",
      "uvlock": true,
      "y": 270
    },
    "facing=east,in_wall=true,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall_open",
      "uvlock": true,
      "y": 270
    },
    "facing=north,in_wall=false,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate",
      "uvlock": true,
      "y": 180
    },
    "facing=north,in_wall=false,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_open",
      "uvlock": true,
      "y": 180
    },
    "facing=north,in_wall=true,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall",
      "uvlock": true,
      "y": 180
    },
    "facing=north,in_wall=true,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall_open",
      "uvlock": true,
      "y": 180
    },
    "facing=south,in_wall=false,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate",
      "uvlock": true
    },
    "facing=south,in_wall=false,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_open",
      "uvlock": true
    },
    "facing=south,in_wall=true,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall",
      "uvlock": true
    },
    "facing=south,in_wall=true,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall_open",
      "uvlock": true
    },
    "facing=west,in_wall=false,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate",
      "uvlock": true,
      "y": 90
    },
    "facing=west,in_wall=false,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_open",
      "uvlock": true,
      "y": 90
    },
    "facing=west,in_wall=true,open=false": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall",
      "uvlock": true,
      "y": 90
    },
    "facing=west,in_wall=true,open=true": {
      "model": "%NS%:block/wood/%WOOD%/fence_gate_wall_open",
      "uvlock": true,
      "y": 90
    }
  }
}
""";

    public static final String FENCE = """
{
  "multipart": [
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/fence_post"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/fence_side",
        "uvlock": true
      },
      "when": {
        "north": "true"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/fence_side",
        "uvlock": true,
        "y": 90
      },
      "when": {
        "east": "true"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/fence_side",
        "uvlock": true,
        "y": 180
      },
      "when": {
        "south": "true"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/fence_side",
        "uvlock": true,
        "y": 270
      },
      "when": {
        "west": "true"
      }
    }
  ]
}
""";

    public static final String HANGING_SIGN = """
{
  "variants": {
    "attached=false,rotation=0": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_0"
    },
    "attached=false,rotation=1": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_1"
    },
    "attached=false,rotation=10": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_2",
      "y": 180
    },
    "attached=false,rotation=11": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_3",
      "y": 180
    },
    "attached=false,rotation=12": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_0",
      "y": 270
    },
    "attached=false,rotation=13": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_1",
      "y": 270
    },
    "attached=false,rotation=14": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_2",
      "y": 270
    },
    "attached=false,rotation=15": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_3",
      "y": 270
    },
    "attached=false,rotation=2": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_2"
    },
    "attached=false,rotation=3": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_3"
    },
    "attached=false,rotation=4": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_0",
      "y": 90
    },
    "attached=false,rotation=5": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_1",
      "y": 90
    },
    "attached=false,rotation=6": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_2",
      "y": 90
    },
    "attached=false,rotation=7": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_3",
      "y": 90
    },
    "attached=false,rotation=8": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_0",
      "y": 180
    },
    "attached=false,rotation=9": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_rot_1",
      "y": 180
    },
    "attached=true,rotation=0": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_0"
    },
    "attached=true,rotation=1": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_1"
    },
    "attached=true,rotation=10": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_2",
      "y": 180
    },
    "attached=true,rotation=11": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_3",
      "y": 180
    },
    "attached=true,rotation=12": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_0",
      "y": 270
    },
    "attached=true,rotation=13": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_1",
      "y": 270
    },
    "attached=true,rotation=14": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_2",
      "y": 270
    },
    "attached=true,rotation=15": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_3",
      "y": 270
    },
    "attached=true,rotation=2": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_2"
    },
    "attached=true,rotation=3": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_3"
    },
    "attached=true,rotation=4": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_0",
      "y": 90
    },
    "attached=true,rotation=5": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_1",
      "y": 90
    },
    "attached=true,rotation=6": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_2",
      "y": 90
    },
    "attached=true,rotation=7": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_3",
      "y": 90
    },
    "attached=true,rotation=8": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_0",
      "y": 180
    },
    "attached=true,rotation=9": {
      "model": "%NS%:block/wood/%WOOD%/hanging_sign_attached_rot_1",
      "y": 180
    }
  }
}
""";

    public static final String LEAVES = """
{
  "variants": {
    "": {
      "model": "%NS%:block/wood/%WOOD%/leaves"
    }
  }
}
""";

    public static final String LOG = """
{
  "variants": {
    "axis=x": {
      "model": "%NS%:block/wood/%WOOD%/log_horizontal",
      "x": 90,
      "y": 90
    },
    "axis=y": {
      "model": "%NS%:block/wood/%WOOD%/log"
    },
    "axis=z": {
      "model": "%NS%:block/wood/%WOOD%/log_horizontal",
      "x": 90
    }
  }
}
""";

    public static final String PLANKS = """
{
  "variants": {
    "": {
      "model": "%NS%:block/wood/%WOOD%/planks"
    }
  }
}
""";

    public static final String PRESSURE_PLATE = """
{
  "variants": {
    "powered=false": {
      "model": "%NS%:block/wood/%WOOD%/pressure_plate"
    },
    "powered=true": {
      "model": "%NS%:block/wood/%WOOD%/pressure_plate_down"
    }
  }
}
""";

    public static final String SAPLING = """
{
  "variants": {
    "": {
      "model": "%NS%:block/wood/%WOOD%/sapling"
    }
  }
}
""";

    public static final String SHELF = """
{
  "multipart": [
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf"
      },
      "when": {
        "facing": "north"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf",
        "y": 90
      },
      "when": {
        "facing": "east"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf",
        "y": 180
      },
      "when": {
        "facing": "south"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf",
        "y": 270
      },
      "when": {
        "facing": "west"
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unpowered"
      },
      "when": {
        "AND": [
          {
            "facing": "north"
          },
          {
            "powered": "false"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unpowered",
        "y": 90
      },
      "when": {
        "AND": [
          {
            "facing": "east"
          },
          {
            "powered": "false"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unpowered",
        "y": 180
      },
      "when": {
        "AND": [
          {
            "facing": "south"
          },
          {
            "powered": "false"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unpowered",
        "y": 270
      },
      "when": {
        "AND": [
          {
            "facing": "west"
          },
          {
            "powered": "false"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unconnected"
      },
      "when": {
        "AND": [
          {
            "facing": "north"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "unconnected"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unconnected",
        "y": 90
      },
      "when": {
        "AND": [
          {
            "facing": "east"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "unconnected"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unconnected",
        "y": 180
      },
      "when": {
        "AND": [
          {
            "facing": "south"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "unconnected"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_unconnected",
        "y": 270
      },
      "when": {
        "AND": [
          {
            "facing": "west"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "unconnected"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_left"
      },
      "when": {
        "AND": [
          {
            "facing": "north"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "left"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_left",
        "y": 90
      },
      "when": {
        "AND": [
          {
            "facing": "east"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "left"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_left",
        "y": 180
      },
      "when": {
        "AND": [
          {
            "facing": "south"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "left"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_left",
        "y": 270
      },
      "when": {
        "AND": [
          {
            "facing": "west"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "left"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_center"
      },
      "when": {
        "AND": [
          {
            "facing": "north"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "center"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_center",
        "y": 90
      },
      "when": {
        "AND": [
          {
            "facing": "east"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "center"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_center",
        "y": 180
      },
      "when": {
        "AND": [
          {
            "facing": "south"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "center"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_center",
        "y": 270
      },
      "when": {
        "AND": [
          {
            "facing": "west"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "center"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_right"
      },
      "when": {
        "AND": [
          {
            "facing": "north"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "right"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_right",
        "y": 90
      },
      "when": {
        "AND": [
          {
            "facing": "east"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "right"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_right",
        "y": 180
      },
      "when": {
        "AND": [
          {
            "facing": "south"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "right"
          }
        ]
      }
    },
    {
      "apply": {
        "model": "%NS%:block/wood/%WOOD%/shelf_right",
        "y": 270
      },
      "when": {
        "AND": [
          {
            "facing": "west"
          },
          {
            "powered": "true"
          },
          {
            "side_chain": "right"
          }
        ]
      }
    }
  ]
}
""";

    public static final String SIGN = """
{
  "variants": {
    "rotation=0": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_0"
    },
    "rotation=1": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_1"
    },
    "rotation=10": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_2",
      "y": 180
    },
    "rotation=11": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_3",
      "y": 180
    },
    "rotation=12": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_0",
      "y": 270
    },
    "rotation=13": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_1",
      "y": 270
    },
    "rotation=14": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_2",
      "y": 270
    },
    "rotation=15": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_3",
      "y": 270
    },
    "rotation=2": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_2"
    },
    "rotation=3": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_3"
    },
    "rotation=4": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_0",
      "y": 90
    },
    "rotation=5": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_1",
      "y": 90
    },
    "rotation=6": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_2",
      "y": 90
    },
    "rotation=7": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_3",
      "y": 90
    },
    "rotation=8": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_0",
      "y": 180
    },
    "rotation=9": {
      "model": "%NS%:block/wood/%WOOD%/sign_rot_1",
      "y": 180
    }
  }
}
""";

    public static final String SLAB = """
{
  "variants": {
    "type=bottom": {
      "model": "%NS%:block/wood/%WOOD%/slab"
    },
    "type=double": {
      "model": "%NS%:block/wood/%WOOD%/planks"
    },
    "type=top": {
      "model": "%NS%:block/wood/%WOOD%/slab_top"
    }
  }
}
""";

    public static final String STAIRS = """
{
  "variants": {
    "facing=east,half=bottom,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "y": 270
    },
    "facing=east,half=bottom,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner"
    },
    "facing=east,half=bottom,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "y": 270
    },
    "facing=east,half=bottom,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer"
    },
    "facing=east,half=bottom,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs"
    },
    "facing=east,half=top,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180
    },
    "facing=east,half=top,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180,
      "y": 90
    },
    "facing=east,half=top,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180
    },
    "facing=east,half=top,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180,
      "y": 90
    },
    "facing=east,half=top,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs",
      "uvlock": true,
      "x": 180
    },
    "facing=north,half=bottom,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "y": 180
    },
    "facing=north,half=bottom,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "y": 270
    },
    "facing=north,half=bottom,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "y": 180
    },
    "facing=north,half=bottom,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "y": 270
    },
    "facing=north,half=bottom,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs",
      "uvlock": true,
      "y": 270
    },
    "facing=north,half=top,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180,
      "y": 270
    },
    "facing=north,half=top,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180
    },
    "facing=north,half=top,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180,
      "y": 270
    },
    "facing=north,half=top,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180
    },
    "facing=north,half=top,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs",
      "uvlock": true,
      "x": 180,
      "y": 270
    },
    "facing=south,half=bottom,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner"
    },
    "facing=south,half=bottom,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "y": 90
    },
    "facing=south,half=bottom,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer"
    },
    "facing=south,half=bottom,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "y": 90
    },
    "facing=south,half=bottom,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs",
      "uvlock": true,
      "y": 90
    },
    "facing=south,half=top,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180,
      "y": 90
    },
    "facing=south,half=top,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180,
      "y": 180
    },
    "facing=south,half=top,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180,
      "y": 90
    },
    "facing=south,half=top,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180,
      "y": 180
    },
    "facing=south,half=top,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs",
      "uvlock": true,
      "x": 180,
      "y": 90
    },
    "facing=west,half=bottom,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "y": 90
    },
    "facing=west,half=bottom,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "y": 180
    },
    "facing=west,half=bottom,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "y": 90
    },
    "facing=west,half=bottom,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "y": 180
    },
    "facing=west,half=bottom,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs",
      "uvlock": true,
      "y": 180
    },
    "facing=west,half=top,shape=inner_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180,
      "y": 180
    },
    "facing=west,half=top,shape=inner_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_inner",
      "uvlock": true,
      "x": 180,
      "y": 270
    },
    "facing=west,half=top,shape=outer_left": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180,
      "y": 180
    },
    "facing=west,half=top,shape=outer_right": {
      "model": "%NS%:block/wood/%WOOD%/stairs_outer",
      "uvlock": true,
      "x": 180,
      "y": 270
    },
    "facing=west,half=top,shape=straight": {
      "model": "%NS%:block/wood/%WOOD%/stairs",
      "uvlock": true,
      "x": 180,
      "y": 180
    }
  }
}
""";

    public static final String TRAPDOOR = """
{
  "variants": {
    "facing=east,half=bottom,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_bottom"
    },
    "facing=east,half=bottom,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open",
      "y": 90
    },
    "facing=east,half=top,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_top"
    },
    "facing=east,half=top,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open",
      "y": 90
    },
    "facing=north,half=bottom,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_bottom"
    },
    "facing=north,half=bottom,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open"
    },
    "facing=north,half=top,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_top"
    },
    "facing=north,half=top,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open"
    },
    "facing=south,half=bottom,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_bottom"
    },
    "facing=south,half=bottom,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open",
      "y": 180
    },
    "facing=south,half=top,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_top"
    },
    "facing=south,half=top,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open",
      "y": 180
    },
    "facing=west,half=bottom,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_bottom"
    },
    "facing=west,half=bottom,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open",
      "y": 270
    },
    "facing=west,half=top,open=false": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_top"
    },
    "facing=west,half=top,open=true": {
      "model": "%NS%:block/wood/%WOOD%/trapdoor_open",
      "y": 270
    }
  }
}
""";

    public static final String WALL_HANGING_SIGN = """
{
  "variants": {
    "facing=east": {
      "model": "%NS%:block/wood/%WOOD%/wall_hanging_sign",
      "y": 270
    },
    "facing=north": {
      "model": "%NS%:block/wood/%WOOD%/wall_hanging_sign",
      "y": 180
    },
    "facing=south": {
      "model": "%NS%:block/wood/%WOOD%/wall_hanging_sign"
    },
    "facing=west": {
      "model": "%NS%:block/wood/%WOOD%/wall_hanging_sign",
      "y": 90
    }
  }
}
""";

    public static final String WALL_SIGN = """
{
  "variants": {
    "facing=east": {
      "model": "%NS%:block/wood/%WOOD%/wall_sign",
      "y": 270
    },
    "facing=north": {
      "model": "%NS%:block/wood/%WOOD%/wall_sign",
      "y": 180
    },
    "facing=south": {
      "model": "%NS%:block/wood/%WOOD%/wall_sign"
    },
    "facing=west": {
      "model": "%NS%:block/wood/%WOOD%/wall_sign",
      "y": 90
    }
  }
}
""";

    public static final String WOOD = """
{
  "variants": {
    "axis=x": {
      "model": "%NS%:block/wood/%WOOD%/wood",
      "x": 90,
      "y": 90
    },
    "axis=y": {
      "model": "%NS%:block/wood/%WOOD%/wood"
    },
    "axis=z": {
      "model": "%NS%:block/wood/%WOOD%/wood",
      "x": 90
    }
  }
}
""";

    public static final String STRIPPED_LOG = """
{
  "variants": {
    "axis=x": {
      "model": "%NS%:block/wood/%WOOD%/stripped_log_horizontal",
      "x": 90,
      "y": 90
    },
    "axis=y": {
      "model": "%NS%:block/wood/%WOOD%/stripped_log"
    },
    "axis=z": {
      "model": "%NS%:block/wood/%WOOD%/stripped_log_horizontal",
      "x": 90
    }
  }
}
""";

    public static final String STRIPPED_WOOD = """
{
  "variants": {
    "axis=x": {
      "model": "%NS%:block/wood/%WOOD%/stripped_wood",
      "x": 90,
      "y": 90
    },
    "axis=y": {
      "model": "%NS%:block/wood/%WOOD%/stripped_wood"
    },
    "axis=z": {
      "model": "%NS%:block/wood/%WOOD%/stripped_wood",
      "x": 90
    }
  }
}
""";

    /** 方块名模板 -> blockstate JSON */
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
        map.put("%s_wall_hanging_sign", WALL_HANGING_SIGN);
        map.put("%s_wall_sign", WALL_SIGN);
        map.put("%s_wood", WOOD);
        map.put("stripped_%s_log", STRIPPED_LOG);
        map.put("stripped_%s_wood", STRIPPED_WOOD);
        TEMPLATES = Collections.unmodifiableMap(map);
    }
}
