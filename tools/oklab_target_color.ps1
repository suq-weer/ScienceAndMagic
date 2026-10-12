#Requires -Version 5.1
<#
.SYNOPSIS
    从红杉设计贴图反推「注册时应当传入的目标色」，并校验各贴图类别之间的相对关系。

.DESCRIPTION
    重着色管线（team.xcmt.sam.util.OkLabMap#matching）做的是：把源贴图的 OKLab <b>加权均值</b>
    仿射对齐到目标色。因此目标色必须是「设计贴图的 OKLab 均值再转回 sRGB」得到的颜色，
    这样才能保证管线复现出来的均值与设计一致。

    本脚本对每张设计贴图输出：
      - 设计贴图的 OKLab 加权均值；
      - 由该均值反推的 sRGB 目标色（可直接写进 ModBlocks）；
      - 原版对应贴图的 OKLab 均值，以及两者之差（用于判断设计是否保留了橡木的相对明暗结构）。

    注意：直接对 sRGB 求平均得到的颜色与「OKLab 均值转 sRGB」并不相同，前者会被 gamma 拖偏。

.NOTES
    原版贴图从 Gradle 下载的 minecraft sources jar 中只读读取；设计贴图从 src/main/resources 读取。
#>
[CmdletBinding()]
param(
    [string]$Jar = 'D:\Minecraft\ScienceAndMagic\build\moddev\artifacts\minecraft-patched-26.2.0.89-sources.jar',
    # 设计贴图只作为「目标色」的实测依据，不再打包进模组，因此放在 tools/reference 下。
    [string]$ModTextureRoot = 'D:\Minecraft\ScienceAndMagic\tools\reference\textures',
    # 锚点类别：主目标色由它决定（原版也是让整套木头以木板为基准色）
    [string]$AnchorTarget = 'block/nature/sequoia/plank.png',
    [string]$AnchorSource = 'block/oak_planks'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem

$Pairs = @(
    [pscustomobject]@{ Key = 'PLANKS';           Source = 'block/oak_planks';          Target = 'block/nature/sequoia/plank.png' }
    [pscustomobject]@{ Key = 'LOG';              Source = 'block/oak_log';             Target = 'block/nature/sequoia/log_side.png' }
    [pscustomobject]@{ Key = 'LOG_TOP';          Source = 'block/oak_log_top';         Target = 'block/nature/sequoia/log_top.png' }
    [pscustomobject]@{ Key = 'STRIPPED_LOG';     Source = 'block/stripped_oak_log';    Target = 'block/nature/sequoia/peeled_log_side.png' }
    [pscustomobject]@{ Key = 'STRIPPED_LOG_TOP'; Source = 'block/stripped_oak_log_top';Target = 'block/nature/sequoia/peeled_log_top.png' }
    [pscustomobject]@{ Key = 'SAPLING';          Source = 'block/oak_sapling';         Target = 'item/nature/sequoia/sapling.png' }
    [pscustomobject]@{ Key = 'ITEM_BOAT';        Source = 'item/oak_boat';             Target = 'item/nature/sequoia/boat.png' }
    [pscustomobject]@{ Key = 'ITEM_CHEST_BOAT';  Source = 'item/oak_chest_boat';       Target = 'item/nature/sequoia/boat_chest.png' }
)

# ---------------------------------------------------------------------------
# 色彩管线：与 team.xcmt.sam.util.OkLab 保持一致
# ---------------------------------------------------------------------------
function Convert-SrgbToLinear([double]$c) {
    if ($c -le 0.04045) { return $c / 12.92 }
    return [Math]::Pow(($c + 0.055) / 1.055, 2.4)
}

function Convert-LinearToSrgb([double]$c) {
    if ($c -le 0.0031308) { return $c * 12.92 }
    return 1.055 * [Math]::Pow($c, 1.0 / 2.4) - 0.055
}

function Convert-LinearToOkLab([double]$lr, [double]$lg, [double]$lb) {
    $l = 0.4122214708 * $lr + 0.5363325363 * $lg + 0.0514459929 * $lb
    $m = 0.2119034982 * $lr + 0.6806995451 * $lg + 0.1073969566 * $lb
    $s = 0.0883024619 * $lr + 0.2817188376 * $lg + 0.6299787005 * $lb

    $lc = [Math]::Sign($l) * [Math]::Pow([Math]::Abs($l), 1.0 / 3.0)
    $mc = [Math]::Sign($m) * [Math]::Pow([Math]::Abs($m), 1.0 / 3.0)
    $sc = [Math]::Sign($s) * [Math]::Pow([Math]::Abs($s), 1.0 / 3.0)

    return @(
        (0.2104542553 * $lc + 0.7936177850 * $mc - 0.0040720468 * $sc),
        (1.9779984951 * $lc - 2.4285922050 * $mc + 0.4505937099 * $sc),
        (0.0259040371 * $lc + 0.7827717662 * $mc - 0.8086757660 * $sc)
    )
}

function Convert-OkLabToLinear([double]$lightness, [double]$a, [double]$b) {
    $lc = $lightness + 0.3963377774 * $a + 0.2158037573 * $b
    $mc = $lightness - 0.1055613458 * $a - 0.0638541728 * $b
    $sc = $lightness - 0.0894841775 * $a - 1.2914855480 * $b

    $l = $lc * $lc * $lc
    $m = $mc * $mc * $mc
    $s = $sc * $sc * $sc

    return @(
        (4.0767416621 * $l - 3.3077115913 * $m + 0.2309699292 * $s),
        (-1.2684380046 * $l + 2.6097574011 * $m - 0.3413193965 * $s),
        (-0.0041960863 * $l - 0.7034186147 * $m + 1.7076147010 * $s)
    )
}

function Get-OkLabMean([System.Drawing.Bitmap]$image) {
    $weightSum = 0.0; $lSum = 0.0; $aSum = 0.0; $bSum = 0.0; $count = 0
    for ($y = 0; $y -lt $image.Height; $y++) {
        for ($x = 0; $x -lt $image.Width; $x++) {
            $pixel = $image.GetPixel($x, $y)
            if ($pixel.A -eq 0) { continue }
            $weight = $pixel.A / 255.0
            $lab = Convert-LinearToOkLab (Convert-SrgbToLinear ($pixel.R / 255.0)) (Convert-SrgbToLinear ($pixel.G / 255.0)) (Convert-SrgbToLinear ($pixel.B / 255.0))
            $lSum += $weight * $lab[0]; $aSum += $weight * $lab[1]; $bSum += $weight * $lab[2]
            $weightSum += $weight; $count++
        }
    }
    if ($weightSum -le 0) { return $null }
    return @(($lSum / $weightSum), ($aSum / $weightSum), ($bSum / $weightSum), $count)
}

function Convert-OkLabMeanToHex([double[]]$lab) {
    $linear = Convert-OkLabToLinear $lab[0] $lab[1] $lab[2]
    $r = [Math]::Round(255 * (Convert-LinearToSrgb ([Math]::Max(0.0, [Math]::Min(1.0, $linear[0])))))
    $g = [Math]::Round(255 * (Convert-LinearToSrgb ([Math]::Max(0.0, [Math]::Min(1.0, $linear[1])))))
    $b = [Math]::Round(255 * (Convert-LinearToSrgb ([Math]::Max(0.0, [Math]::Min(1.0, $linear[2])))))
    return ('#{0:X2}{1:X2}{2:X2}' -f [int]$r, [int]$g, [int]$b)
}

function Get-VanillaImage([string]$relativePath, [hashtable]$entries) {
    $entryName = 'assets/minecraft/textures/' + $relativePath + '.png'
    $entry = $entries[$entryName]
    if ($null -eq $entry) { return $null }
    $stream = $entry.Open()
    $memory = New-Object System.IO.MemoryStream
    $stream.CopyTo($memory)
    $memory.Position = 0
    $stream.Dispose()
    return [System.Drawing.Bitmap]::FromStream($memory)
}

function Get-ModImage([string]$relativePath) {
    $full = Join-Path $ModTextureRoot ($relativePath -replace '/', [IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path $full)) { return $null }
    return [System.Drawing.Bitmap]::FromFile($full)
}

$zip = [System.IO.Compression.ZipFile]::OpenRead($Jar)
try {
    $entries = @{}
    foreach ($entry in $zip.Entries) {
        if ($entry.FullName.StartsWith('assets/minecraft/textures/')) {
            $entries[$entry.FullName] = $entry
        }
    }

    Write-Output ''
    Write-Output '=========================================================================================='
    Write-Output ' 红杉设计贴图 OKLab 均值 / 反推目标色 / 与原版橡木的相对关系'
    Write-Output '=========================================================================================='

    $rows = New-Object 'System.Collections.Generic.List[object]'
    foreach ($pair in $Pairs) {
        $target = Get-ModImage $pair.Target
        $source = Get-VanillaImage $pair.Source $entries
        if ($null -eq $target) { Write-Warning ('缺少设计贴图: ' + $pair.Target); continue }

        $targetLab = Get-OkLabMean $target
        $targetHex = Convert-OkLabMeanToHex $targetLab

        $sourceLab = $null
        if ($null -ne $source) { $sourceLab = Get-OkLabMean $source }

        $deltaL = ''
        $deltaA = ''
        $deltaB = ''
        $sourceLabText = 'n/a'
        if ($null -ne $sourceLab) {
            $deltaL = '{0:+.4f}' -f ($targetLab[0] - $sourceLab[0])
            $deltaA = '{0:+.4f}' -f ($targetLab[1] - $sourceLab[1])
            $deltaB = '{0:+.4f}' -f ($targetLab[2] - $sourceLab[2])
            $sourceLabText = '{0:N4}, {1:N4}, {2:N4}' -f $sourceLab[0], $sourceLab[1], $sourceLab[2]
        }

        $rows.Add([pscustomobject]@{
            Key        = $pair.Key
            Size       = ('{0}x{1}' -f $target.Width, $target.Height)
            Pixels     = $targetLab[3]
            DesignLab  = ('{0:N4}, {1:N4}, {2:N4}' -f $targetLab[0], $targetLab[1], $targetLab[2])
            TargetHex  = $targetHex
            SourceLab  = $sourceLabText
            DeltaL     = $deltaL
            DeltaA     = $deltaA
            DeltaB     = $deltaB
        })

        $target.Dispose()
        if ($null -ne $source) { $source.Dispose() }
    }

    foreach ($row in $rows) {
        Write-Output ('{0,-16} {1,-7} px={2,-5} 设计OKLab=({3})  目标色={4}' -f `
            $row.Key, $row.Size, $row.Pixels, $row.DesignLab, $row.TargetHex)
        Write-Output ('{0,-16} {1,-7}        原版OKLab=({2})  设计-原版 Δ=({3}, {4}, {5})' -f `
            '', '', $row.SourceLab, $row.DeltaL, $row.DeltaA, $row.DeltaB)
    }

    $anchor = $rows | Where-Object { $_.Key -eq 'PLANKS' } | Select-Object -First 1
    Write-Output ''
    if ($null -ne $anchor) {
        Write-Output ('推荐传入注册的目标色（锚点 ' + $AnchorTarget + '）：' + $anchor.TargetHex)
        Write-Output ('  对应设计贴图 OKLab 均值：' + $anchor.DesignLab)
        Write-Output ('  原版 ' + $AnchorSource + ' OKLab 均值：' + $anchor.SourceLab)
    }
    else {
        Write-Warning ('未找到锚点设计贴图 ' + $AnchorTarget + '，无法推荐目标色')
    }
    Write-Output ''
}
finally {
    $zip.Dispose()
}
