# DoctorService Docker 部署脚本
# 使用方式: .\deploy.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DoctorService Docker 部署脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Docker 是否运行
Write-Host "[1/5] 检查 Docker Desktop..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✓ Docker Desktop 正在运行" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker Desktop 未运行，请先启动 Docker Desktop" -ForegroundColor Red
    exit 1
}

# 切换到 DoctorService 目录
Write-Host ""
Write-Host "[2/5] 切换到 DoctorService 目录..." -ForegroundColor Yellow
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath
Write-Host "✓ 当前目录: $scriptPath" -ForegroundColor Green

# 停止并删除旧容器（如果存在）
Write-Host ""
Write-Host "[3/5] 清理旧容器..." -ForegroundColor Yellow
$container = docker ps -a -q -f name=doctor-service
if ($container) {
    docker stop doctor-service 2>$null
    docker rm doctor-service 2>$null
    Write-Host "✓ 已删除旧容器" -ForegroundColor Green
} else {
    Write-Host "○ 没有旧容器需要清理" -ForegroundColor Gray
}

# 构建镜像
Write-Host ""
Write-Host "[4/5] 构建 Docker 镜像..." -ForegroundColor Yellow
Write-Host "  这可能需要几分钟时间，请耐心等待..." -ForegroundColor Gray
docker build -t doctor-service:latest .
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 镜像构建成功" -ForegroundColor Green
} else {
    Write-Host "✗ 镜像构建失败" -ForegroundColor Red
    exit 1
}

# 启动容器
Write-Host ""
Write-Host "[5/5] 启动容器..." -ForegroundColor Yellow
docker run -d `
    --name doctor-service `
    -p 8081:8081 `
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:63333/hospital_new `
    -e SPRING_DATASOURCE_USERNAME=root `
    -e SPRING_DATASOURCE_PASSWORD=HGY25qiu@shixun `
    -e SPRING_DATA_REDIS_HOST=host.docker.internal `
    -e SPRING_DATA_REDIS_PORT=6379 `
    -e SPRING_WEB_CORS_ALLOWED_ORIGINS=http://localhost:5173 `
    --restart unless-stopped `
    doctor-service:latest

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 容器启动成功" -ForegroundColor Green
} else {
    Write-Host "✗ 容器启动失败" -ForegroundColor Red
    exit 1
}

# 等待服务启动
Write-Host ""
Write-Host "等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 显示容器状态
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "容器状态:" -ForegroundColor Yellow
docker ps -f name=doctor-service --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

Write-Host ""
Write-Host "查看日志: docker logs -f doctor-service" -ForegroundColor Cyan
Write-Host "停止服务: docker stop doctor-service" -ForegroundColor Cyan
Write-Host "重启服务: docker restart doctor-service" -ForegroundColor Cyan
Write-Host ""
Write-Host "服务地址: http://localhost:8081" -ForegroundColor Green
Write-Host "测试接口: http://localhost:8081/doctor/test/update-schedule-status" -ForegroundColor Green
Write-Host ""
