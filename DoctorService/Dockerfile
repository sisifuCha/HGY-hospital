# 使用官方的OpenJDK 17-jdk-slim作为基础镜像（稳定可靠）
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将Maven构建的jar包复制到容器中
COPY target/DoctorService-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用程序端口
EXPOSE 8081

# 运行应用程序
ENTRYPOINT ["java", "-jar", "app.jar"]