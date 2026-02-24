# 베이스 이미지
FROM amazoncorretto:17 AS junyoung-builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 설정 파일만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 소스 코드가 없어도 의존성(라이브러리)은 미리 받을 수 있다.
# 미리 라이브러리만 땡겨 온다.
RUN ./gradlew dependencies --no-daemon

# 소스코드 실제 복사
COPY src src

# 도커 이미지는 실행에 필요한 .jar만 있으면 되기에 bootJar 사용
RUN ./gradlew bootJar -x test --no-daemon

# 실행 환경 (실제 운영 이미지)
FROM amazoncorretto:17-alpine

WORKDIR /app

# 실행 환경에 curl 설치 - alpine이라 없을수도 있음.
RUN apk add --no-cache curl

# 빌드된 JAR 파일만 복사
COPY --from=junyoung-builder /app/build/libs/app.jar app.jar

# 프로젝트 정보 - 환경 변수
ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

# 80 포트 노출
EXPOSE 80

# 실행
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar app.jar"]