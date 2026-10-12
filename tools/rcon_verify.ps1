<#
.SYNOPSIS
    极简 RCON 客户端，用于对开发服务端做功能冒烟测试（例如验证地物能否放置）。

.DESCRIPTION
    RunServer 的 Gradle 任务不会转发 stdin，所以无法用管道喂命令；
    这里直接用 RCON 协议（TCP，小端）连上去执行命令并打印回显。

.EXAMPLE
    pwsh -File tools/rcon_verify.ps1 -Password samverify -CommandFile cmds.txt
#>
param(
    [string]$RconHost = '127.0.0.1',
    [int]$RconPort = 25575,
    [string]$Password = 'samverify',
    [string]$CommandFile = ''
)

$ErrorActionPreference = 'Stop'

function Send-Packet($stream, [int]$id, [int]$type, [string]$body) {
    $payload = [System.Text.Encoding]::ASCII.GetBytes($body)
    $length = 4 + 4 + $payload.Length + 2
    $ms = New-Object System.IO.MemoryStream
    $bw = New-Object System.IO.BinaryWriter($ms)
    $bw.Write([int]$length)
    $bw.Write([int]$id)
    $bw.Write([int]$type)
    $bw.Write($payload)
    $bw.Write([byte]0)
    $bw.Write([byte]0)
    $bw.Flush()
    $bytes = $ms.ToArray()
    $stream.Write($bytes, 0, $bytes.Length)
    $stream.Flush()
    $bw.Dispose()
}

function Read-Packet($stream) {
    $lenBuf = New-Object byte[] 4
    $read = 0
    while ($read -lt 4) {
        $n = $stream.Read($lenBuf, $read, 4 - $read)
        if ($n -le 0) { throw 'connection closed' }
        $read += $n
    }
    $len = [System.BitConverter]::ToInt32($lenBuf, 0)
    if ($len -lt 10) { throw "bad packet length $len" }
    $buf = New-Object byte[] $len
    $read = 0
    while ($read -lt $len) {
        $n = $stream.Read($buf, $read, $len - $read)
        if ($n -le 0) { throw 'connection closed' }
        $read += $n
    }
    return [pscustomobject]@{
        Id   = [System.BitConverter]::ToInt32($buf, 0)
        Type = [System.BitConverter]::ToInt32($buf, 4)
        Body = [System.Text.Encoding]::ASCII.GetString($buf, 8, $len - 10)
    }
}

$client = New-Object System.Net.Sockets.TcpClient
$client.Connect($RconHost, $RconPort)
$stream = $client.GetStream()
$stream.ReadTimeout = 20000

Send-Packet $stream 1 3 $Password
$auth = $null
for ($i = 0; $i -lt 3; $i++) {
    $auth = Read-Packet $stream
    if ($auth.Id -eq 1) { break }
}
if ($null -eq $auth -or $auth.Id -ne 1) { throw 'RCON authentication failed' }
Write-Output 'RCON authenticated'

$commands = [System.IO.File]::ReadAllLines($CommandFile) | Where-Object { $_.Trim().Length -gt 0 }
$id = 10
foreach ($cmd in $commands) {
    Send-Packet $stream $id 2 $cmd
    $resp = $null
    do { $resp = Read-Packet $stream } while ($resp.Id -ne $id)
    Write-Output ("CMD  {0}" -f $cmd)
    Write-Output ("  -> {0}" -f $resp.Body.Trim())
    $id++
}

$client.Close()
