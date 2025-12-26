FROM amazoncorretto:17-alpine-jdk

# # 方案C：使用 Eclipse Temurin（推荐的企业级JDK）
# FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY target/ManagerService-0.0.1-SNAPSHOT.jar /app/ManagerService.jar

EXPOSE 8080

CMD ["java", "-jar", "ManagerService.jar"]