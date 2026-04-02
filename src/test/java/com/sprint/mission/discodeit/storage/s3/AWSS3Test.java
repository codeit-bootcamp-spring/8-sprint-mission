package com.sprint.mission.discodeit.storage.s3;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Disabled
@SpringBootTest(properties = {
    "discodeit.storage.s3.access-key=dummy-key",
    "discodeit.storage.s3.secret-key=dummy-secret",
    "discodeit.storage.s3.region=ap-northeast-2",
    "discodeit.storage.s3.bucket=dummy-bucket"
})
@ActiveProfiles("test")
public class AWSS3Test {

  @MockitoBean
  private S3Client s3Client;
  @MockitoBean
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


  @Test
  @DisplayName("S3 업로드 Mock 테스트")
  void uploadTest() {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName).key("goat.png").contentType("image/png").build();

    s3Client.putObject(putObjectRequest, RequestBody.fromString("가짜 이미지 데이터"));
    System.out.println("업로드 성공!");
  }

  @Test
  void presignedUrlTest() throws Exception {
    var mockResponse = mock(
        software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest.class);

    when(mockResponse.url()).thenReturn(java.net.URI.create("http://fake-s3-url.com").toURL());

    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(mockResponse);

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(java.time.Duration.ofMinutes(10))
        .getObjectRequest(GetObjectRequest.builder().bucket(bucketName).key("goat.png").build())
        .build();

    String url = s3Presigner.presignGetObject(presignRequest).url().toString();
    System.out.println("생성된 URL: " + url);
  }

  @Test
  @DisplayName("S3 다운로드 Mock 테스트")
  void downloadTest() {
    when(s3Client.getObject(any(GetObjectRequest.class)))
        .thenReturn(mock(software.amazon.awssdk.core.ResponseInputStream.class));

    s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key("goat.png").build());
    System.out.println("다운로드 시도 성공!");
  }
}