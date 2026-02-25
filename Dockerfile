FROM amazoncorretto:17 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew
# 의존성 패키지만 미리 다운로드
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사 및 실제 빌드
COPY . .
RUN ./gradlew build -x test --no-daemon

FROM amazoncorretto:17-alpine
WORKDIR /app

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

COPY --from=builder /app/build/libs/*.jar $PROJECT_NAME-$PROJECT_VERSION.jar

EXPOSE 80

CMD ["sh", "-c", "java $JVM_OPTS -jar $PROJECT_NAME-$PROJECT_VERSION.jar"]

# ===================================================================
# 빌드 및 실행 예시:
# 
# 1. 애플리케이션 빌드:
# ./gradlew build
# 
# 2. Docker 이미지 빌드:
# docker build -t menu-app:basic .
# 
# 3. 컨테이너 실행:
# docker run -p 8888:8080 menu-app:basic
# 
# 4. 애플리케이션 접속 확인:
# curl http://localhost:8888/actuator/health
# =================================================================== 