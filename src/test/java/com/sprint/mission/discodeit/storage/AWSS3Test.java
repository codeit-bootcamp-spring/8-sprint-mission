package com.sprint.mission.discodeit.storage;

import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;


@SpringBootTest(classes = AWSS3Test.class)
@Disabled // 로컬/CI 환경에 AWS 키가 없을 때 테스트 제외
@Slf4j
public class AWSS3Test {

  @Value("${AWS_S3_ACCESS_KEY}")
  private String accessKey;

  @Value("${AWS_S3_SECRET_KEY}")
  private String secretKey;

  @Value("${AWS_S3_REGION}")
  private String region;

  @Value("${AWS_S3_BUCKET}")
  private String bucketName;

  private S3Client s3Client;
  private S3Presigner s3Presigner;

  @BeforeEach
  void setUp() {
    // 자격증명
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Test
  @DisplayName("S3 파일 업로드 테스트")
  void uploadTest() throws IOException {
    String key = "test-folder/hello.txt";
    String content = "Hello S3 from Sprint Mission!";

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType("text/plain")
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromString(content));
    log.info("Upload complete: {}", key);
  }

  @Test
  @DisplayName("S3 파일 다운로드 테스트")
  void downloadTest() {
    String key = "test-folder/hello.txt";

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
    log.info("Downloaded content: {}", new String(objectBytes.asByteArray()));
  }

  @Test
  @DisplayName("Presigned URL 생성 테스트")
  void generatePresignedUrlTest() {
    String key = "test-folder/hello.txt";

    // 1. URL 생성을 위한 GetObjectRequest 정의
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    // 2. Presign 요청 정의 (유효 시간 설정 포함)
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10)) // 10분간 유효한 URL 생성
        .getObjectRequest(getObjectRequest)
        .build();

    // 3. S3Presigner를 사용하여 실제 URL 생성
    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

    log.info("Generated Presigned URL: {}", presignedRequest.url());
  }
}
