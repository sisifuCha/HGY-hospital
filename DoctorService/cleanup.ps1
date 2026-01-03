# DoctorService Docker 清理脚本
# 使用方式: .\cleanup.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DoctorService Docker 清理脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 停止容器
Write-Host "[1/3] 停止容器..." -ForegroundColor Yellow
$container = docker ps -q -f name=doctor-service
if ($container) {
    docker stop doctor-service
    Write-Host "✓ 容器已停止" -ForegroundColor Green
} else {
    Write-Host "○ 容器未运行" -ForegroundColor Gray
}

# 删除容器
Write-Host ""
Write-Host "[2/3] 删除容器..." -ForegroundColor Yellow
$container = docker ps -a -q -f name=doctor-service
if ($container) {
    docker rm doctor-service
    Write-Host "✓ 容器已删除" -ForegroundColor Green
} else {
    Write-Host "○ 没有容器需要删除" -ForegroundColor Gray
}

# 删除镜像（可选）
Write-Host ""
Write-Host "[3/3] 删除镜像..." -ForegroundColor Yellow
$response = Read-Host "是否删除镜像？(y/N)"
if ($response -eq "y" -or $response -eq "Y") {
    docker rmi doctor-service:latest 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ 镜像已删除" -ForegroundColor Green
    } else {
        Write-Host "○ 没有镜像需要删除" -ForegroundColor Gray
    }
} else {
    Write-Host "○ 保留镜像" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  清理完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
