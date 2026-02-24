package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class AWSS3Test {

  private S3Client s3Client;
  private S3Presigner s3Presigner;
  private String bucketName;

  @BeforeEach
  @DisplayName("Properties를 활용한 .env 로드 및 클라이언트 초기화")
  void setUp() throws IOException {
    // Properties 클래스 사용, .env 파일 로드
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    }

    // 환경 변수 추출 및 사용
    String accessKey = props.getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = props.getProperty("AWS_S3_SECRET_KEY");
    String regionStr = props.getProperty("AWS_S3_REGION");
    this.bucketName = props.getProperty("AWS_S3_BUCKET");

    Region region = Region.of(regionStr);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey)
    );

    // S3Client 및 S3Presigner 초기화
    this.s3Client = S3Client.builder()
        .region(region)
        .credentialsProvider(credentialsProvider)
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(region)
        .credentialsProvider(credentialsProvider)
        .build();
  }

  @Test
  @DisplayName("S3 파일 업로드 테스트")
  void uploadTest() {
    String key = "test/jun.txt";
    String content = "Hello Im jun!";

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType("text/plain")
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromString(content));

    HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    assertDoesNotThrow(() -> s3Client.headObject(headObjectRequest));
  }

  @Test
  @DisplayName("S3 파일 다운로드 테스트")
  void downloadTest() {
    String key = "test/jun.txt";

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

    byte[] data = objectBytes.asByteArray();

    assertNotNull(data);
    assertTrue(data.length > 0);
  }

  @Test
  @DisplayName("Presigned URL 생성 테스트")
  void presignedUrlTest() {
    String key = "test/jun.txt";

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    // 임시 발행 URL 조건 생성
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .getObjectRequest(getObjectRequest)
        .build();

    // 실제 임시 발행 URL 생성
    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

    String url = presignedRequest.url().toString();

    System.out.printf("생성 된 Presigned URL: %s", url);
    assertNotNull(url);
    assertTrue(url.contains(bucketName));
  }
}
