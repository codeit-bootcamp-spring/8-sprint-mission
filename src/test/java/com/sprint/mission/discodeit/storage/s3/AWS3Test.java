package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
public class AWS3Test {

  private S3Client s3Client;
  private S3Presigner s3Presigner;
  private String bucketName;
  // 객체 키
  private final String TEST_FILE_KEY = "test-folder/hello.txt";

  @BeforeEach
  void setUp() throws IOException {
    // 환경 변수 우선 로드
    String accessKey = System.getenv("AWS_S3_ACCESS_KEY");
    String secretKey = System.getenv("AWS_S3_SECRET_KEY");
    String regionStr = System.getenv("AWS_S3_REGION");
    this.bucketName = System.getenv("AWS_S3_BUCKET");

    // 환경 변수가 없는 경우 .env 로드
    if (accessKey == null || secretKey == null) {
      Properties properties = new Properties();
      try (FileInputStream fis = new FileInputStream(".env")) {
        properties.load(fis);
      }
      accessKey = properties.getProperty("AWS_S3_ACCESS_KEY");
      secretKey = properties.getProperty("AWS_S3_SECRET_KEY");
      regionStr = properties.getProperty("AWS_S3_REGION");
      this.bucketName = properties.getProperty("AWS_S3_BUCKET");
    }

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    Region region = Region.of(regionStr);

    this.s3Client = S3Client.builder()
        .region(region)
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(region)
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Test
  @DisplayName("upload: 지정된 경로(key)로 문자열 데이터를 객체로 저장할 수 있다.")
  void upload() {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(TEST_FILE_KEY)
        .build();
    s3Client.putObject(putObjectRequest, RequestBody.fromString("Hello, Discodeit S3 Test"));
    log.info("S3 객체 업로드 완료. Key: {}", TEST_FILE_KEY);
  }

  @Test
  @DisplayName("download: 버킷에 저장된 객체를 조회하여 업로드된 내용과 일치하는지 확인 가능하다.")
  void download() {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(TEST_FILE_KEY)
        .build();

    String downloadText = s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
    log.info("S3 객체 다운로드 완료. downloadText: {}", downloadText);

    assertNotNull(downloadText, "다운로드 된 텍스트는 null이 될 수 없습니다.");
  }

  @Test
  @DisplayName("generatePresignedUrl: 특정 객체에 대해 10분간 유효한 임시 접근 URL을 생성할 수 있다.")
  void generatePresignedUrl() {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(TEST_FILE_KEY)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
        presignRequest);
    String url = presignedGetObjectRequest.url().toString();

    log.info("Presigned URL 생성 성공. url: {}", url);

    assertNotNull(url, "생성된 Presigned URL은 null이 될 수 없습니다.");
  }

  @AfterEach
  void tearDown() {
    try {
      if (s3Client != null) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(TEST_FILE_KEY)
            .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("S3 테스트 객체 삭제 완료. Key: {}", TEST_FILE_KEY);
      }
    } catch (Exception e) {
      log.error("S3 테스트 객체 삭제 중 오류 발생: {}", e.getMessage());
    } finally {
      // 리소스 해제
      if (s3Client != null) {
        s3Client.close();
      }
      if (s3Presigner != null) {
        s3Presigner.close();
      }
      log.info("S3 클린업 및 리소스 해제 완료");
    }
  }
}
