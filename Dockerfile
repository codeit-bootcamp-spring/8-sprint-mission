# 1. Amazon Corretto 17버전(OpenJDK 기반)을 베이스 이미지로 사용.
FROM amazoncorretto:17

# 2. 컨테이너 내부에서 명령어를 실행할 작업 디렉토리를 /app으로 설정.
WORKDIR /app

# 3. Gradle 래퍼 실행 파일을 컨테이너의 현재 폴더(/app)로 복사.
COPY gradlew .
# 4. Gradle 설정 및 라이브러리 정보가 담긴 gradle 폴더를 복사.
COPY gradle gradle
# 5. 의존성 라이브러리 정보가 적힌 빌드 설정 파일을 복사.
COPY build.gradle .
# 6. 프로젝트 설정 정보가 적힌 파일을 복사.
COPY settings.gradle .
# 7. 실제 자바 소스 코드가 들어있는 src 폴더를 복사.
COPY src src

# 8. gradlew에 실행 권한을 주고, 테스트를 제외한 전체 프로젝트 빌드를 수행.
RUN chmod +x gradlew && ./gradlew build -x test --no-daemon

# 9. 빌드 결과물인 jar 파일의 이름을 맞추기 위해 프로젝트명을 환경 변수로 설정.
ENV PROJECT_NAME=discodeit
# 10. 프로젝트 버전을 환경 변수로 설정.
ENV PROJECT_VERSION=1.2-M8
# 11. 힙 메모리 크기 등 자바 가상 머신(JVM) 옵션을 담을 변수를 생성.
ENV JVM_OPTS=""

# 12. 컨테이너가 80번 포트를 통해 외부와 통신함을 명시.
EXPOSE 80

# 13. 컨테이너 시작 시 환경 변수들을 조합하여 최종 jar 파일을 실행.
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar"]