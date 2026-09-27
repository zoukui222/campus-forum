# ========== 构建阶段 ==========
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先只复制 pom.xml，让依赖下载单独成层：只改业务代码时不会重新下载依赖
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ========== 运行阶段 ==========
FROM eclipse-temurin:17-jre
WORKDIR /app

# 时区统一为 Asia/Shanghai，避免容器日志与数据库时间相差 8 小时
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=builder /build/target/*.jar app.jar

# 上传文件目录，由 docker volume 挂载以持久化（容器重建不丢图）
RUN mkdir -p /app/file

EXPOSE 8080

# 小内存服务器上限制堆大小，避免 JVM 吃满内存被系统 OOM Kill
ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseSerialGC -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
