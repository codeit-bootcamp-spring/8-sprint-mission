# 베이스 이미지
FROM amazoncorretto:17 AS junyoung-builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일 복사
# 명시적으로 적어줘도 되긴 하지만 .dockerignore에 파일 들 정의 할것
# ignore에 명시된 파일 제외한 모든 것들이 /app으로 복사 된다.
COPY . .

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 도커 이미지는 실행에 필요한 .jar만 있으면 되기에 bootJar 사용
RUN ./gradlew clean bootJar -x test

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