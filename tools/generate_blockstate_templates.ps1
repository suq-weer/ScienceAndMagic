<#
.SYNOPSIS
    从 Minecraft 源码 jar 提取原版橡木方块状态 JSON，转换为「木种共享 blockstate 模板」并生成 Java 源文件。

.DESCRIPTION
    每个木种拥有自己的一套薄模型（%NS%:block/wood/%WOOD%/<model>，parent 指向原版几何模板），
    但 blockstate 的「形状」与木种无关，只是模型路径里带了木种名。
    因此这里把原版 oak_* 的 blockstate 转换一次（把模型路径换成带占位符的形式），
    并以 Java 文本块常量形式写入 WoodBlockStateTemplates，供 datagen 为每个木种展开输出。
    占位符 %NS% / %WOOD% 由 team.xcmt.sam.datagen.TemplatedJsonProvider 替换。

.EXAMPLE
    pwsh -File tools/generate_blockstate_templates.ps1
#>
param(
    [string]$Jar = 'D:\Minecraft\ScienceAndMagic\build\moddev\artifacts\minecraft-patched-26.2.0.89-sources.jar',
    [string]$ProjectRoot = 'D:\Minecraft\ScienceAndMagic',
    [string]$Base = 'oak',
    [string]$Namespace = 'science_and_magic',
    [string]$Package = 'team.xcmt.sam.datagen'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$zip = [System.IO.Compression.ZipFile]::OpenRead($Jar)

function Read-Entry([string]$name) {
    $e = $zip.GetEntry($name)
    if ($null -eq $e) { throw "jar entry not found: $name" }
    $r = New-Object System.IO.StreamReader($e.Open())
    try { return $r.ReadToEnd() } finally { $r.Dispose() }
}

$entries = @($zip.Entries |
        Where-Object { $_.FullName -match "^assets/minecraft/blockstates/(stripped_)?$Base(_[^/]+)?\.json$" } |
        Select-Object -ExpandProperty FullName | Sort-Object)

if ($entries.Count -eq 0) { throw "no vanilla blockstates found for base '$Base'" }

# 每木种一套薄模型：命名空间与木种名都留成占位符，由 TemplatedJsonProvider 展开。
$modelNs = "%NS%:block/wood/%WOOD%/"

$constants = @()
$mappings = @()

foreach ($entry in $entries) {
    $leaf = [System.IO.Path]::GetFileNameWithoutExtension($entry)
    $json = Read-Entry $entry
    # 先替换 stripped_ 前缀，再替换普通前缀，避免误伤
    $json = $json.Replace("minecraft:block/stripped_$Base" + "_", ($modelNs + "stripped_"))
    $json = $json.Replace("minecraft:block/$Base" + "_", $modelNs)
    $json = ($json -replace "`r`n", "`n").Trim()

    if ($leaf -match "^stripped_$Base") {
        $rest = $leaf.Substring(("stripped_" + $Base).Length).TrimStart('_')
        $const = "STRIPPED_" + $rest.ToUpperInvariant()
        $nameTemplate = "stripped_%s_" + $rest
    }
    else {
        $rest = $leaf.Substring($Base.Length).TrimStart('_')
        $const = $rest.ToUpperInvariant()
        $nameTemplate = "%s_" + $rest
    }

    $constants += [pscustomobject]@{ Const = $const; Json = $json }
    $mappings += [pscustomobject]@{ Const = $const; Template = $nameTemplate }
}

$zip.Dispose()

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine("package $Package;")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("import java.util.Collections;")
[void]$sb.AppendLine("import java.util.LinkedHashMap;")
[void]$sb.AppendLine("import java.util.Map;")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("/**")
[void]$sb.AppendLine(" * 由 tools/generate_blockstate_templates.ps1 自动生成，请勿手工修改。")
[void]$sb.AppendLine(" * <p>")
[void]$sb.AppendLine(" * 木种的 blockstate 模板：键为方块名模板（{@code %s} 代表木种名），值为 blockstate JSON 内容。")
[void]$sb.AppendLine(" * 模型路径写成 {@code %NS%:block/wood/%WOOD%/<model>}，由 {@link TemplatedJsonProvider} 展开。")
[void]$sb.AppendLine(" */")
[void]$sb.AppendLine("public final class WoodBlockStateTemplates {")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("    private WoodBlockStateTemplates() {")
[void]$sb.AppendLine("    }")
[void]$sb.AppendLine("")
foreach ($c in $constants) {
    [void]$sb.AppendLine('    public static final String ' + $c.Const + ' = """')
    [void]$sb.Append($c.Json)
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine('""";')
    [void]$sb.AppendLine("")
}
[void]$sb.AppendLine("    /** 方块名模板 -> blockstate JSON */")
[void]$sb.AppendLine("    public static final Map<String, String> TEMPLATES;")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("    static {")
[void]$sb.AppendLine("        Map<String, String> map = new LinkedHashMap<>();")
foreach ($m in $mappings) {
    [void]$sb.AppendLine("        map.put(""$($m.Template)"", $($m.Const));")
}
[void]$sb.AppendLine("        TEMPLATES = Collections.unmodifiableMap(map);")
[void]$sb.AppendLine("    }")
[void]$sb.AppendLine("}")

$outDir = Join-Path $ProjectRoot ("src/main/java/" + ($Package -replace '\.', '/'))
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir -Force | Out-Null }
$outFile = Join-Path $outDir 'WoodBlockStateTemplates.java'
[System.IO.File]::WriteAllText($outFile, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))

Write-Output "blockstate templates: $($constants.Count)"
Write-Output "output: $outFile"
