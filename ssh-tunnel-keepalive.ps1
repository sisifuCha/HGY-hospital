# SSH 隧道自动重连脚本
# 功能：维护 SSH 端口转发，自动重连

param(
    [int]$MaxRetries = 0,  # 0 表示无限重试
    [int]$RetryDelay = 5   # 重试间隔（秒）
)

$sshHost = "124.71.238.8"
$sshUser = "root"
$sshPassword = "HGY25qiu@shixun"
$localPort = "63333"
$remoteHost = "192.168.0.43"
$remotePort = "5432"

$attemptCount = 0

Write-Host "=== SSH 隧道自动重连服务 ===" -ForegroundColor Green
Write-Host "本地端口: 0.0.0.0:$localPort" -ForegroundColor Cyan
Write-Host "远程目标: $remoteHost:$remotePort" -ForegroundColor Cyan
Write-Host "SSH 服务器: $sshUser@$sshHost" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示: 按 Ctrl+C 停止服务" -ForegroundColor Yellow
Write-Host ""

while ($true) {
    $attemptCount++
    
    if ($MaxRetries -gt 0 -and $attemptCount -gt $MaxRetries) {
        Write-Host "达到最大重试次数 ($MaxRetries)，退出" -ForegroundColor Red
        break
    }
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] 尝试 #$attemptCount - 建立 SSH 隧道..." -ForegroundColor Yellow
    
    # SSH 命令参数：
    # -L: 端口转发
    # -N: 不执行远程命令
    # -o ServerAliveInterval=30: 每30秒发送保活包
    # -o ServerAliveCountMax=3: 最多3次无响应后断开
    # -o TCPKeepAlive=yes: 启用 TCP 保活
    # -o ExitOnForwardFailure=yes: 端口转发失败时退出
    # -o StrictHostKeyChecking=no: 不检查主机密钥（首次连接）
    
    $process = Start-Process -FilePath "ssh" `
        -ArgumentList @(
            "-L", "0.0.0.0:${localPort}:${remoteHost}:${remotePort}",
            "-N",
            "-o", "ServerAliveInterval=30",
            "-o", "ServerAliveCountMax=3",
            "-o", "TCPKeepAlive=yes",
            "-o", "ExitOnForwardFailure=yes",
            "-o", "StrictHostKeyChecking=no",
            "${sshUser}@${sshHost}"
        ) `
        -PassThru `
        -Wait
    
    $exitCode = $process.ExitCode
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    
    if ($exitCode -eq 0) {
        Write-Host "[$timestamp] SSH 隧道正常关闭" -ForegroundColor Green
        break
    } else {
        Write-Host "[$timestamp] SSH 隧道断开 (退出码: $exitCode)" -ForegroundColor Red
        Write-Host "[$timestamp] 等待 $RetryDelay 秒后重连..." -ForegroundColor Yellow
        Start-Sleep -Seconds $RetryDelay
    }
}

Write-Host ""
Write-Host "SSH 隧道服务已停止" -ForegroundColor Red
