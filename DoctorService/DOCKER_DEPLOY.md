# DoctorService Docker 部署指南

## 前置条件
- ✅ Docker Desktop 已安装并运行
- ✅ PostgreSQL 数据库运行在 localhost:63333
- ✅ Redis 运行在 localhost:6379（如需要）

## 快速开始

### 方式一：使用 Docker Compose（推荐）

1. **启动服务**
```powershell
cd DoctorService
docker-compose up -d
```

2. **查看日志**
```powershell
docker-compose logs -f doctor-service
```

3. **停止服务**
```powershell
docker-compose down
```

### 方式二：使用 Docker 命令

#### 1. 构建镜像
```powershell
cd DoctorService
docker build -t doctor-service:latest .
```

#### 2. 运行容器
```powershell
docker run -d `
  --name doctor-service `
  -p 8081:8081 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:63333/hospital_new `
  -e SPRING_DATASOURCE_USERNAME=root `
  -e SPRING_DATASOURCE_PASSWORD=HGY25qiu@shixun `
  -e SPRING_DATA_REDIS_HOST=host.docker.internal `
  -e SPRING_DATA_REDIS_PORT=6379 `
  --restart unless-stopped `
  doctor-service:latest
```

## 常用 Docker 命令

### 查看容器状态
```powershell
docker ps
```

### 查看容器日志
```powershell
docker logs -f doctor-service
```

### 停止容器
```powershell
docker stop doctor-service
```

### 启动已停止的容器
```powershell
docker start doctor-service
```

### 删除容器
```powershell
docker rm -f doctor-service
```

### 查看镜像
```powershell
docker images
```

### 删除镜像
```powershell
docker rmi doctor-service:latest
```

### 进入容器内部（调试用）
```powershell
docker exec -it doctor-service sh
```

## 验证部署

### 1. 检查容器是否运行
```powershell
docker ps | Select-String doctor-service
```

### 2. 测试服务健康状态
```powershell
curl http://localhost:8081/doctor/test/update-schedule-status
```

### 3. 查看实时日志
```powershell
docker logs -f doctor-service
```

## 配置说明

### 环境变量
- `SPRING_DATASOURCE_URL`: 数据库连接地址
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `SPRING_DATA_REDIS_HOST`: Redis 主机地址
- `SPRING_DATA_REDIS_PORT`: Redis 端口
- `JAVA_OPTS`: JVM 参数（内存、GC等）

### 连接宿主机服务
Docker 容器内使用 `host.docker.internal` 来访问宿主机的服务：
- 数据库：`host.docker.internal:63333`
- Redis：`host.docker.internal:6379`

## 故障排查

### 容器无法启动
```powershell
# 查看容器日志
docker logs doctor-service

# 查看详细状态
docker inspect doctor-service
```

### 无法连接数据库
1. 确认 PostgreSQL 正在运行：`netstat -an | Select-String 63333`
2. 检查防火墙设置
3. 验证数据库允许来自 Docker 的连接

### 容器运行但无法访问
```powershell
# 检查端口映射
docker port doctor-service

# 测试容器内部
docker exec doctor-service wget -O- http://localhost:8081/actuator/health
```

## 镜像优化

当前 Dockerfile 使用了多阶段构建：
- **构建阶段**：Maven 3.9 + JDK 21
- **运行阶段**：JRE 21 Alpine（轻量级）
- **镜像大小**：约 300MB

## 生产环境建议

1. **安全性**
   - 使用非 root 用户运行（已配置）
   - 定期更新基础镜像
   - 不在镜像中硬编码密码

2. **性能**
   - 根据负载调整 JVM 内存参数
   - 使用健康检查确保服务可用性
   - 配置日志轮转避免磁盘占满

3. **监控**
   - 集成 Prometheus + Grafana
   - 配置告警规则
   - 记录关键业务指标

## 重新构建

如果修改了代码，需要重新构建：

```powershell
# 停止并删除旧容器
docker-compose down

# 重新构建并启动
docker-compose up -d --build
```

或使用 Docker 命令：
```powershell
docker stop doctor-service
docker rm doctor-service
docker rmi doctor-service:latest
docker build -t doctor-service:latest .
docker run -d ...（使用上面的运行命令）
```

## 端口说明

- **8081**: DoctorService HTTP 端口
- **63333**: PostgreSQL 数据库端口（宿主机）
- **6379**: Redis 端口（宿主机）
