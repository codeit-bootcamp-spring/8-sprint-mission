package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.config.S3Properties;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
public class S3BinaryContentStorageTest {

  private S3BinaryContentStorage storage;
  private Properties envProperties;
  private UUID currentTestId;
  private String bucketName;

  private S3Client s3Client;
  private S3Presigner s3Presigner;

  @BeforeEach
  void setUp() throws IOException {
    String accessKey = getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = getProperty("AWS_S3_SECRET_KEY");
    String regionStr = getProperty("AWS_S3_REGION");
    this.bucketName = getProperty("AWS_S3_BUCKET");

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
    Region region = Region.of(regionStr);

    this.s3Client = S3Client.builder()
        .region(region)
        .credentialsProvider(credentialsProvider)
        .build();

    this.s3Presigner = S3Presigner.builder()
        .region(region)
        .credentialsProvider(credentialsProvider)
        .build();

    S3Properties s3Properties = new S3Properties(accessKey, secretKey, regionStr, this.bucketName);

    this.currentTestId = UUID.randomUUID();

    this.storage = new S3BinaryContentStorage(s3Properties, s3Client, s3Presigner);
  }

  @AfterEach
  void tearDown() {
    if (storage != null && currentTestId != null) {
      try {
        storage.delete(currentTestId);
      } catch (Exception e) {
        log.error("클린업 실패: " + e.getMessage());
      }
    }

    if (s3Client != null) {
      s3Client.close();
    }
    if (s3Presigner != null) {
      s3Presigner.close();
    }
  }

  @Test
  @DisplayName("put: 바이너리 데이터를 S3에 성공적으로 업로드할 수 있다.")
  void putTest() {
    // given
    byte[] content = "Hello Put Test".getBytes();

    // when
    UUID id = storage.put(currentTestId, content);

    // then
    assertEquals(currentTestId, id);
    log.info("S3 객체 업로드 성공: id: {}", id);
  }

  @Test
  @DisplayName("get: 업로드된 데이터를 S3에서 정상적으로 조회할 수 있다.")
  void getTest() throws IOException {
    // given
    byte[] expectedContent = "Hello Get Test".getBytes();
    storage.put(currentTestId, expectedContent);

    // when
    InputStream inputStream = storage.get(currentTestId);

    // then
    assertNotNull(inputStream);
    byte[] actualContent = inputStream.readAllBytes();
    assertEquals(new String(expectedContent), new String(actualContent));
    log.info("S3 객체 조회 성공: inputStream: {}", inputStream);
  }

  @Test
  @DisplayName("download: Presigned URL을 통해 리다이렉트 응답을 생성한다.")
  void downloadTest() {
    // given
    storage.put(currentTestId, "Hello Download Test".getBytes());
    BinaryContentDto dto = createBinaryContentDto(currentTestId);

    // when
    ResponseEntity<Void> response = storage.download(dto);

    // then
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertNotNull(response.getHeaders().getLocation());

    log.info("Presigned URL 리다이렉트 생성 성공(URL: : {}", response.getHeaders().getLocation());
  }

  private BinaryContentDto createBinaryContentDto(UUID id) {
    return new BinaryContentDto(
        id,
        "test-file",
        1024L,
        "text/plain"
    );
  }

  private String getProperty(String key) throws IOException {
    String value = System.getenv(key);
    if (value != null) {
      return value;
    }

    if (envProperties == null) {
      envProperties = new Properties();
      try (FileInputStream fis = new FileInputStream(".env")) {
        envProperties.load(fis);
      }
    }

    return envProperties.getProperty(key);
  }
}
