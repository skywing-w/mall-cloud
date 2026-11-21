# 基础镜像：项目编译目标为 Java 17，基础镜像必须同为 17，否则 jar 无法启动（UnsupportedClassVersionError）
FROM openjdk:17-jre-slim
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 声明服务端口，便于 docker run -P 自动映射
EXPOSE 8081
# 拷贝jar包
COPY mall-service.jar /app.jar
# 入口
ENTRYPOINT ["java", "-jar", "/app.jar"]