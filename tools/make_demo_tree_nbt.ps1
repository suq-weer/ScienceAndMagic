# 生成一个示范用的结构蓝图（StructureTemplate NBT，gzip 压缩）。
#
# 用途：
#   1. 造出一个合法的树形蓝图：丢进 src/main/resources/gen/tree_shapes/<预设>/<N>x<N>/ 就会被 runData 采纳；
#   2. 构造负例（-TrunkSize 小于桶目录的 N），用来验证 runData 的「树干足迹」校验确实会失败。
#
# 关键约定（见 TemplateTreeShape / TemplateAnchors 的注释）：
#   * 树干用 minecraft:oak_log 标记，树叶用 minecraft:oak_leaves 标记，放置时按木种替换；
#   * 锚点由代码做「树干足迹匹配」：在最低的「存在完整 NxN 实心木质方块」的层里，
#     取上一层支撑度最高的那个 NxN 方格的西北角。所以树干在包围盒里的位置随意；
#   * 空气不会被写进蓝图（等价于结构空位）。
# 这里让树干位于 x,z ∈ [2,4] 而不是紧贴原点，正是为了验证锚点不是靠「局部原点」得来的。
#
# 注意默认输出位置：它会为 conifer 重建 3x3 桶。只是试手的话，用 -Out 指到别处。
#
# 负例用法（会被 runData 拒绝，报「树干足迹容不下 3x3」）：
#   pwsh -File tools/make_demo_tree_nbt.ps1 -TrunkSize 2 `
#        -Out src/main/resources/gen/tree_shapes/fancy/3x3/bad.nbt
#
# 注意：NBT 是**大端**格式，而 .NET 的 BinaryWriter 是小端，所以这里手写字节，不用 BinaryWriter。
param(
    [string]$Out = 'src/main/resources/gen/tree_shapes/conifer/3x3/tower.nbt',
    [int]$TrunkSize = 3,
    [int]$TrunkHeight = 6
)

$ErrorActionPreference = 'Stop'

$sizeX = 7
$sizeY = 9
$sizeZ = 7
$center = 3

# 树干在水平面上居中，左上角（西北角）随 TrunkSize 变化
$trunkX0 = $center - [Math]::Floor(($TrunkSize - 1) / 2)
$trunkZ0 = $trunkX0
$trunkX1 = $trunkX0 + $TrunkSize
$trunkZ1 = $trunkZ0 + $TrunkSize

$blocks = New-Object System.Collections.ArrayList

# 树干（调色板索引 0 = oak_log）
for ($y = 0; $y -lt $TrunkHeight; $y++) {
    for ($x = $trunkX0; $x -lt $trunkX1; $x++) {
        for ($z = $trunkZ0; $z -lt $trunkZ1; $z++) {
            [void]$blocks.Add(@($x, $y, $z, 0))
        }
    }
}

# 树冠：以 ($center,*, $center) 为中心的锥形（调色板索引 1 = oak_leaves）
for ($y = $TrunkHeight - 1; $y -lt $sizeY; $y++) {
    $radius = [Math]::Max(0, 3 - [Math]::Floor(($y - ($TrunkHeight - 1)) / 1.5))
    for ($x = 0; $x -lt $sizeX; $x++) {
        for ($z = 0; $z -lt $sizeZ; $z++) {
            $dx = $x - $center
            $dz = $z - $center
            if (($dx * $dx + $dz * $dz) -le ($radius * $radius + 1)) {
                # 树干范围内不覆盖木头
                $insideTrunk = ($x -ge $trunkX0 -and $x -lt $trunkX1 -and $z -ge $trunkZ0 -and $z -lt $trunkZ1 -and $y -lt $TrunkHeight)
                if (-not $insideTrunk) {
                    [void]$blocks.Add(@($x, $y, $z, 1))
                }
            }
        }
    }
}

$buffer = New-Object System.Collections.Generic.List[byte]

function Add-Byte {
    param([int]$Value)
    $buffer.Add([byte]($Value -band 0xFF))
}

# 大端 2 字节
function Add-Short {
    param([int]$Value)
    Add-Byte ([int](($Value -shr 8) -band 0xFF))
    Add-Byte ([int]($Value -band 0xFF))
}

# 大端 4 字节
function Add-Int {
    param([int]$Value)
    Add-Byte ([int](($Value -shr 24) -band 0xFF))
    Add-Byte ([int](($Value -shr 16) -band 0xFF))
    Add-Byte ([int](($Value -shr 8) -band 0xFF))
    Add-Byte ([int]($Value -band 0xFF))
}

# NBT 字符串：2 字节大端长度 + UTF-8 字节
function Add-NbtString {
    param([string]$Value)
    $utf8 = [System.Text.Encoding]::UTF8.GetBytes($Value)
    Add-Short $utf8.Length
    $buffer.AddRange($utf8)
}

function Add-NbtHeader {
    param([int]$Type, [string]$Name)
    Add-Byte $Type
    Add-NbtString $Name
}

function Add-NbtEnd {
    Add-Byte 0
}

# 根 TAG_Compound（根标签的名字会被读取并丢弃，这里用空名）
Add-NbtHeader 10 ''

# size: TAG_List<TAG_Int>，长度 3
Add-NbtHeader 9 'size'
Add-Byte 3
Add-Int 3
Add-Int $sizeX
Add-Int $sizeY
Add-Int $sizeZ

# palette: TAG_List<TAG_Compound>，每项一个 "Name"
Add-NbtHeader 9 'palette'
Add-Byte 10
Add-Int 2
foreach ($name in @('minecraft:oak_log', 'minecraft:oak_leaves')) {
    Add-NbtHeader 8 'Name'
    Add-NbtString $name
    Add-NbtEnd
}

# blocks: TAG_List<TAG_Compound>，每项 {pos: List<Int>[3], state: Int}
Add-NbtHeader 9 'blocks'
Add-Byte 10
Add-Int $blocks.Count
foreach ($block in $blocks) {
    Add-NbtHeader 9 'pos'
    Add-Byte 3
    Add-Int 3
    Add-Int ([int]$block[0])
    Add-Int ([int]$block[1])
    Add-Int ([int]$block[2])
    Add-NbtHeader 3 'state'
    Add-Int ([int]$block[3])
    Add-NbtEnd
}

# entities: 空列表
Add-NbtHeader 9 'entities'
Add-Byte 10
Add-Int 0

Add-NbtEnd

$raw = $buffer.ToArray()

# 自检：开头必须是 0A 00 00 09 00 04 73 69 7A 65（TAG_Compound / 空名 / TAG_List / "size"）
$expect = @(0x0A, 0x00, 0x00, 0x09, 0x00, 0x04, 0x73, 0x69, 0x7A, 0x65)
for ($i = 0; $i -lt $expect.Count; $i++) {
    if ($raw[$i] -ne $expect[$i]) {
        throw ("NBT 头部自检失败：第 " + $i + " 字节应为 " + $expect[$i].ToString('X2') + "，实际 " + $raw[$i].ToString('X2'))
    }
}

$full = Join-Path (Get-Location) $Out
New-Item -ItemType Directory -Force -Path (Split-Path $full -Parent) | Out-Null
$file = [System.IO.File]::Create($full)
$gzip = New-Object System.IO.Compression.GZipStream($file, [System.IO.Compression.CompressionMode]::Compress)
$gzip.Write($raw, 0, $raw.Length)
$gzip.Dispose()
$file.Dispose()

Write-Output ("wrote $full  树干=${TrunkSize}x${TrunkSize}（x,z ∈ [$trunkX0,$($trunkX1 - 1)]，y ∈ [0,$($TrunkHeight - 1)]）  方块数=" + $blocks.Count + "  尺寸=" + $sizeX + "x" + $sizeY + "x" + $sizeZ)
