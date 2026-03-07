package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@SpringBootTest
@ActiveProfiles("test")
public class AWSS3Test {

  private S3Client s3Client;
  private S3Presigner s3Presigner;

  // test.yml이나 application.yml에서 주입받음
  @Value("${discodeit.storage.s3.access-key}")
  private String accessKey;

  @Value("${discodeit.storage.s3.secret-key}")
  private String secretKey;

  @Value("${discodeit.storage.s3.region}")
  private String region;

  @Value("${discodeit.storage.s3.bucket}")
  private String bucketName;

  @BeforeEach
  void setUp() {
    //.env 파일을 직접 읽지 않고 @Value로 받은 값을 사용
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
  void uploadTest() {
    //파일 대신 가상의 바이트 데이터를 업로드
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key("goat.png")
        .contentType("image/png")
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromString("가짜 이미지 데이터"));
    System.out.println("업로드 성공!");
  }

  @Test
  void presignedUrlTest() {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key("goat.png")
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .getObjectRequest(getObjectRequest)
        .build();

    String url = s3Presigner.presignGetObject(presignRequest).url().toString();
    System.out.println("생성된 URL: " + url);
  }

  @Test
  void downloadTest() {
    // S3에서 데이터를 가져와서 출력이 잘 되는지 확인
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key("goat.png")
        .build();

    // 실제 파일로 저장하지 않고 응답이 오는지 확인
    s3Client.getObject(getObjectRequest);
    System.out.println("다운로드 시도 성공!");
  }
}