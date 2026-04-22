# 빌드 스테이지
FROM amazoncorretto:17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 파일 먼저 복사
COPY gradle ./gradle
COPY gradlew ./gradlew

# Gradle 캐시를 위한 의존성 파일 복사
COPY build.gradle settings.gradle ./

# 의존성 다운로드
RUN ./gradlew dependencies

# 소스 코드 복사 및 빌드
COPY src ./src
# 실행용 bootJar를 고정 이름으로 복사해 런타임 스테이지에서 안정적으로 참조
RUN ./gradlew build -x test && cp $(ls -1 /app/build/libs/*.jar | grep -v -- '-plain.jar$' | head -n 1) /app/app.jar


# 런타임 스테이지
FROM amazoncorretto:17-alpine3.21

# 작업 디렉토리 설정
WORKDIR /app

# JVM 옵션만 ENV로 설정
ENV JVM_OPTS=""

# 빌드 스테이지에서 실행 jar 파일 복사
COPY --from=builder /app/app.jar ./app.jar

# 80 포트 노출
EXPOSE 80

# jar 파일 실행
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar app.jar"]
