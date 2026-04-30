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

ENV JVM_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV TZ=Asia/Seoul

COPY --from=builder /app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]