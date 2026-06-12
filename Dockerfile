# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package \
    && cp "$(find target -maxdepth 1 -name '*.jar' ! -name '*.original.jar' | head -n 1)" /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

RUN useradd -r -u 10001 -g root appuser \
    && mkdir -p /app /data/qsl-tracker/files /data/qsl-tracker/logs \
    && chown -R 10001:0 /app /data/qsl-tracker

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 10010

USER 10001

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
