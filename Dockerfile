# 1. 빌드 단계
FROM gradle:8.7-jdk17-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사 (Gradle 래퍼와 프로젝트 파일 포함)
COPY --chown=gradle:gradle . .

# 의존성 캐시 활용을 위해 먼저 빌드 시도 (테스트 제외)
RUN ./gradlew build -x test --no-daemon

# 2. 실행 단계
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 jar 파일 복사
# 빌드된 파일명은 build.gradle의 version과 project name에 따라 결정됨
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 포트 노출 (기본값 8080)
EXPOSE 8080

# 환경 변수 설정 (필요에 따라 변경 가능)
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
