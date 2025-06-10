# Build stage
FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle
RUN gradle dependencies --no-daemon --refresh-dependencies --stacktrace --info \
    || (rm -rf ~/.gradle/caches/ && gradle dependencies --no-daemon --stacktrace)
COPY src /app/src
RUN gradle build -x test --no-daemon --stacktrace || (rm -rf ~/.gradle/caches/ && gradle build -x test --no-daemon --stacktrace)

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache tzdata curl wget
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
COPY src/main/resources/env.yml /app/env.yml
EXPOSE 8987
RUN mkdir -p /app/logs
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=dev", \
    "-Duser.timezone=Asia/Seoul", \
    "-XX:+UseG1GC", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/app/logs/heapdump.hprof", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"] 