# 1. Build Stage
FROM gradle:8.12.1-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
# Build using the gradle image directly, avoiding the missing wrapper issue
RUN gradle build --no-daemon -x test

# 2. Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
