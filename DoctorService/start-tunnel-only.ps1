# SSH 隧道独立启动脚本
# 使用完整的 DoctorService JAR，但通过环境变量只启用 SSH 隧道

Write-Host "=== 启动 SSH 隧道服务 ===" -ForegroundColor Green
Write-Host "仅维护数据库端口转发，不提供 HTTP 服务" -ForegroundColor Yellow
Write-Host ""

$jarPath = "target/DoctorService-0.0.1-SNAPSHOT.jar"

if (-not (Test-Path $jarPath)) {
    Write-Host "错误: 找不到 $jarPath" -ForegroundColor Red
    Write-Host "请先运行: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

Write-Host "启动参数:" -ForegroundColor Cyan
Write-Host "  SSH 隧道: 0.0.0.0:63333 -> 124.71.238.8 -> 192.168.0.43:5432" -ForegroundColor Gray
Write-Host "  HTTP 服务: 禁用 (可选配置)" -ForegroundColor Gray
Write-Host ""
Write-Host "提示: 按 Ctrl+C 停止隧道" -ForegroundColor Yellow
Write-Host ""

# 启动 Java 程序，设置配置让它不启动 Web 服务器
& 'C:\Program Files\Microsoft\jdk-21\bin\java.exe' `
    -Dserver.port=-1 `
    -Dspring.main.web-application-type=none `
    -jar $jarPath

Write-Host ""
Write-Host "SSH 隧道已关闭" -ForegroundColor Red
