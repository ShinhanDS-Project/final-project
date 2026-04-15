# 1. Build Stage
FROM gradle:jdk17 AS build
WORKDIR /home/final-project
COPY --chown=gradle:gradle . .
# Build using the gradle image directly, avoiding the missing wrapper issue
RUN gradle build --no-daemon -x test

# 2. Run Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/final-project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
