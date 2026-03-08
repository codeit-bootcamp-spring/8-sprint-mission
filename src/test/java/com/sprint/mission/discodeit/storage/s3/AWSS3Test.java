package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sprint.mission.discodeit.storage.S3Properties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Disabled
@SpringBootTest(classes = {S3Properties.class})
@EnableConfigurationProperties(S3Properties.class)
public class AWSS3Test {

  @Autowired
  private S3Properties s3Properties;

  private S3Client s3Client;
  private S3Presigner s3Presigner;

  @BeforeEach
  void setUp() {

    AwsBasicCredentials credentials = AwsBasicCredentials.create(
        s3Properties.getAccessKey(),
        s3Properties.getSecretKey()
    );

    s3Client = S3Client.builder()
        .region(Region.of(s3Properties.getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();

    s3Presigner = S3Presigner.builder()
        .region(Region.of(s3Properties.getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }

  @Test
  @DisplayName("업로드 테스트")
  void uploadTest() {
    String key = "hello.txt";
    String content = "This is a test content";

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key)
        .contentType("text/plain")
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromString(content));

    System.out.println("업로드 완료: " + key);
  }

  @Test
  @DisplayName("다운로드 테스트")
  void downloadTest() throws IOException {
    String key = "hello.txt";

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key)
        .build();

    ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
    byte[] contentBytes = response.readAllBytes();
    String content = new String(contentBytes, StandardCharsets.UTF_8);

    System.out.println("다운로드 완료: " + content);
    assertEquals("This is a test content", content);
  }

  @Test
  @DisplayName("Presigned 생성 테스트")
  void presignedTest() {
    String key = "hello.txt";

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(s3Properties.getPresignedUrlExpiration()))
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

    String url = presignedRequest.url().toString();
    System.out.println("생성된 Presigned URL: " + url);
  }
}
