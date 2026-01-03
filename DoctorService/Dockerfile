# DoctorService Dockerfile
# 使用微软提供的 OpenJDK 镜像（国内访问更快）

FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

# 设置工作目录
WORKDIR /app

# 复制已构建的 jar 包
COPY target/DoctorService-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8081

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
