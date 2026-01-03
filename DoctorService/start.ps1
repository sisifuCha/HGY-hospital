# DoctorService 快速启动脚本（无需 Docker）
# 适用于已经用 Maven 构建好 JAR 包的情况

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DoctorService 快速启动" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 JAR 文件是否存在
$jarFile = "target\DoctorService-0.0.1-SNAPSHOT.jar"

if (!(Test-Path $jarFile)) {
    Write-Host "JAR 文件不存在，开始构建..." -ForegroundColor Yellow
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ 构建失败" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✓ JAR 文件已就绪: $jarFile" -ForegroundColor Green
Write-Host ""

# 启动应用
Write-Host "启动 DoctorService..." -ForegroundColor Yellow
Write-Host "端口: 8081" -ForegroundColor Cyan
Write-Host "按 Ctrl+C 停止服务" -ForegroundColor Gray
Write-Host ""

java -Xms256m -Xmx512m -jar $jarFile
