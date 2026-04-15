# 1. 빌드 단계 (Build Stage)
FROM gradle:jdk17 AS build

WORKDIR /home/gradle/src

# 소스 복사 (권한 포함)
COPY --chown=gradle:gradle . .

# 빌드 실행 (테스트 제외)
# 만약 에러가 계속된다면 gradle 대신 ./gradlew를 사용하는 것이 더 안전합니다.
RUN gradle build --no-daemon -x test

# 2. 실행 단계 (Run Stage)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 단계에서 생성된 jar 파일만 복사
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]