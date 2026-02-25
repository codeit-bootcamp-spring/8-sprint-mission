package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class AWS3Test {

  private S3Client s3Client;
  private S3Presigner s3Presigner;
  private String bucketName;
  // 객체 키
  private final String Test_FILE_KEY = "test-folder/hello.txt";

  @BeforeEach
  void setUp() throws IOException {
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      properties.load(fis);
    }

    String accessKey = properties.getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = properties.getProperty("AWS_S3_SECRET_KEY");
    String regionStr = properties.getProperty("AWS_S3_REGION");
    this.bucketName = properties.getProperty("AWS_S3_BUCKET");

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
  void uploadTest() {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(Test_FILE_KEY)
        .build();
    s3Client.putObject(putObjectRequest, RequestBody.fromString("Hello, Discodeit S3 Test"));
    System.out.println("업로드 성공: " + Test_FILE_KEY);
  }

  @Test
  void downloadTest() {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(Test_FILE_KEY)
        .build();

    String downloadText = s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
    System.out.println("다운로드 성공: " + downloadText);

    assertNotNull(downloadText);
  }

  @Test
  void generatePresignedUrlTest() {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(Test_FILE_KEY)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
        presignRequest);
    String url = presignedGetObjectRequest.url().toString();

    System.out.println("Presigned URL 생성 성공: " + url);

    assertNotNull(url);
  }
}
