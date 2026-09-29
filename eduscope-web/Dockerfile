# syntax=docker/dockerfile:1

# Maven Wrapper로 실행 JAR을 만드는 Build Stage.
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -DskipTests clean package


# JDK가 아닌 JRE만 포함한 Runtime Stage.
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install --yes --no-install-recommends openssh-client \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system eduscope \
    && useradd --system --gid eduscope --create-home eduscope \
    && mkdir --parents /app/logs /home/eduscope/.ssh \
    && chown --recursive eduscope:eduscope /app /home/eduscope

WORKDIR /app

COPY --from=builder --chown=eduscope:eduscope \
    /workspace/target/eduscope-web-*.jar /app/app.jar

USER eduscope

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Actuator는 ADMIN 보호 상태이므로 인증 없는 TCP 방식으로 상태를 확인한다.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT:-8080}"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
