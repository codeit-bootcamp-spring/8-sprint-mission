# 스테이지 1 : 빌드
FROM amazoncorretto:17 AS build
WORKDIR /app

# 레이어 캐시 최적화
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 라이브러리 의존성 미리 내려받기
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 이제 실제 소스 코드를 복사하고 빌드
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# 스테이지 2 : 런타임
FROM amazoncorretto:17-alpine AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/discodeit-1.2-M8.jar app.jar

# 환경 변수 및 포트 설정
ENV JVM_OPTS=""
EXPOSE 80

# jar만 실행
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar app.jar"]