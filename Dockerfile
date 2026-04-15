# 1. 빌드 단계 (Build Stage)
FROM gradle:8.12.1-jdk17 AS build
WORKDIR /home/gradle/src

# 빌드에 필요한 소스 복사
COPY --chown=gradle:gradle . .

# 인텔리제이에서 만든 프로젝트를 리눅스용으로 빌드 (테스트 제외)
# 'gradle' 명령어를 직접 사용하여 빌드 파일을 생성합니다.
RUN gradle build --no-daemon -x test

# 2. 실행 단계 (Run Stage)
# 용량이 작고 안정적인 배포 전용 이미지 사용
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드 단계에서 생성된 '진짜' 실행용 jar 파일만 쏙 뽑아오기
# (*-plain.jar 파일은 복사되지 않도록 제외하는 전략)
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar

# 서버 포트 노출
EXPOSE 8080

# 스프링 부트 실행 (운영 서버용 프로필 적용)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]