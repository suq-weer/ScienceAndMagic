<#
.SYNOPSIS
    从 Minecraft 源码 jar 提取原版橡木配方 / 战利品表，转换为「木种共享模板」并生成 Java 源文件。

.DESCRIPTION
    转换规则（%NS% 为命名空间，%WOOD% 为木种名，运行时替换）：
      minecraft:blocks/oak   -> %NS%:blocks/%WOOD%
      minecraft:stripped_oak -> %NS%:stripped_%WOOD%
      minecraft:oak          -> %NS%:%WOOD%
    这样 #minecraft:oak_logs 会变成 #%NS%:%WOOD%_logs（需要自行定义该标签）。

.EXAMPLE
    pwsh -File tools/generate_datagen_templates.ps1
#>
param(
    [string]$Jar = 'D:\Minecraft\ScienceAndMagic\build\moddev\artifacts\minecraft-patched-26.2.0.89-sources.jar',
    [string]$ProjectRoot = 'D:\Minecraft\ScienceAndMagic',
    [string]$Base = 'oak',
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

function Convert-Ids([string]$json) {
    $j = $json -replace "`r`n", "`n"
    $j = $j.Replace("minecraft:blocks/$Base", "%NS%:blocks/%WOOD%")
    $j = $j.Replace("minecraft:stripped_$Base", "%NS%:stripped_%WOOD%")
    $j = $j.Replace("minecraft:$Base", "%NS%:%WOOD%")
    return $j.Trim()
}

function Get-Names([string]$leaf) {
    if ($leaf -match "^stripped_$Base") {
        $rest = $leaf.Substring(("stripped_" + $Base).Length).TrimStart('_')
        return [pscustomobject]@{
            Const    = "STRIPPED_" + $rest.ToUpperInvariant()
            Template = "stripped_%s_" + $rest
        }
    }
    $rest = $leaf.Substring($Base.Length).TrimStart('_')
    return [pscustomobject]@{
        Const    = $rest.ToUpperInvariant()
        Template = "%s_" + $rest
    }
}

function Write-TemplateClass([string]$Class, [string]$Doc, [object[]]$entries) {
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine("package $Package;")
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("import java.util.Collections;")
    [void]$sb.AppendLine("import java.util.LinkedHashMap;")
    [void]$sb.AppendLine("import java.util.Map;")
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("/**")
    [void]$sb.AppendLine(" * 由 tools/generate_datagen_templates.ps1 自动生成，请勿手工修改。")
    [void]$sb.AppendLine(" * <p>")
    [void]$sb.AppendLine(" * $Doc")
    [void]$sb.AppendLine(" */")
    [void]$sb.AppendLine("public final class $Class {")
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("    private $Class() {")
    [void]$sb.AppendLine("    }")
    [void]$sb.AppendLine("")
    foreach ($c in $entries) {
        [void]$sb.AppendLine('    public static final String ' + $c.Const + ' = """')
        [void]$sb.Append($c.Json)
        [void]$sb.AppendLine("")
        [void]$sb.AppendLine('""";')
        [void]$sb.AppendLine("")
    }
    [void]$sb.AppendLine("    /** 名称模板（%s 为木种名）-> JSON 内容 */")
    [void]$sb.AppendLine("    public static final Map<String, String> TEMPLATES;")
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("    static {")
    [void]$sb.AppendLine("        Map<String, String> map = new LinkedHashMap<>();")
    foreach ($c in $entries) {
        [void]$sb.AppendLine('        map.put("' + $c.Template + '", ' + $c.Const + ');')
    }
    [void]$sb.AppendLine("        TEMPLATES = Collections.unmodifiableMap(map);")
    [void]$sb.AppendLine("    }")
    [void]$sb.AppendLine("}")
    return $sb.ToString()
}

$outDir = Join-Path $ProjectRoot ("src/main/java/" + ($Package -replace '\.', '/'))
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir -Force | Out-Null }

# ---- 配方 ----
$recipeEntries = @()
foreach ($entry in @($zip.Entries | Where-Object { $_.FullName -match "^data/minecraft/recipe/(stripped_)?$Base(_[^/]+)?\.json$" } | Select-Object -ExpandProperty FullName | Sort-Object)) {
    $leaf = [System.IO.Path]::GetFileNameWithoutExtension($entry)
    $names = Get-Names $leaf
    $recipeEntries += [pscustomobject]@{ Const = $names.Const; Template = $names.Template; Json = (Convert-Ids (Read-Entry $entry)) }
}
[System.IO.File]::WriteAllText((Join-Path $outDir 'WoodRecipeTemplates.java'),
    (Write-TemplateClass 'WoodRecipeTemplates' '木种共享的合成配方模板（内容与木种无关，仅 id 需要替换）。' $recipeEntries),
    (New-Object System.Text.UTF8Encoding($false)))

# ---- 战利品表 ----
$lootEntries = @()
foreach ($entry in @($zip.Entries | Where-Object { $_.FullName -match "^data/minecraft/loot_table/blocks/(stripped_)?$Base(_[^/]+)?\.json$" } | Select-Object -ExpandProperty FullName | Sort-Object)) {
    $leaf = [System.IO.Path]::GetFileNameWithoutExtension($entry)
    $names = Get-Names $leaf
    $lootEntries += [pscustomobject]@{ Const = $names.Const; Template = $names.Template; Json = (Convert-Ids (Read-Entry $entry)) }
}
[System.IO.File]::WriteAllText((Join-Path $outDir 'WoodLootTemplates.java'),
    (Write-TemplateClass 'WoodLootTemplates' '木种共享的方块战利品表模板（内容与木种无关，仅 id 需要替换）。' $lootEntries),
    (New-Object System.Text.UTF8Encoding($false)))

$zip.Dispose()

Write-Output "recipe templates: $($recipeEntries.Count)"
Write-Output "loot templates: $($lootEntries.Count)"
Write-Output "output: $outDir"
