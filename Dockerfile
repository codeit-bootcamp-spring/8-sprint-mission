# 1단계: Build 스테이지 (빌드 전용)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# [심화] 레이어 캐시 최적화: 종속성 파일을 먼저 복사하여 캐싱 활용
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

# 나머지 소스 복사 및 빌드
COPY src src
RUN ./gradlew clean bootJar --no-daemon

# 2단계: Run 스테이지 (실행 전용 - 슬림 이미지)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일만 가져오기
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]