# 多阶段构建：构建产物在 CI 产出，这里只负责组装运行镜像
# docker build -f deploy/docker/Dockerfile --build-arg JAR_FILE=company-services/order-service/target/order-service-1.0.0-SNAPSHOT.jar -t order-service:1.0.0 .
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY ${JAR_FILE} app.jar

# 统一 JVM 基线：容器感知、随机化哈希种子、OOM 快速失败
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -XX:+UseG1GC" \
    TZ=Asia/Shanghai

USER app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
