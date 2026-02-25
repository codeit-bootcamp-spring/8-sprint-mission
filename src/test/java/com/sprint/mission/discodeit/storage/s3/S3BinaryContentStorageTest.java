package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

//테스트 순서를 부여해서 PUT -> GET -> DOWNLOAD TEST 실행
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class S3BinaryContentStorageTest {

  private S3BinaryContentStorage storage;
  private final UUID TEST_ID = UUID.randomUUID();
  private final String TEST_TEXT = "Hello S3BinaryContentStorage Test";

  @BeforeEach
  void setUp() throws IOException {
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      properties.load(fis);
    }

    String accessKey = properties.getProperty("AWS_S3_ACCESS_KEY");
    String secretKey = properties.getProperty("AWS_S3_SECRET_KEY");
    String regionStr = properties.getProperty("AWS_S3_REGION");
    String bucketName = properties.getProperty("AWS_S3_BUCKET");

    this.storage = new S3BinaryContentStorage(accessKey, secretKey, regionStr, bucketName);
  }

  @Test
  @Order(1)
  void putTest() {
    byte[] bytes = TEST_TEXT.getBytes();
    UUID id = storage.put(TEST_ID, bytes);

    assertEquals(TEST_ID, id);
    System.out.println("S3 객체 업로드 성공: " + id);
  }

  @Test
  @Order(2)
  void getTest() throws IOException {
    InputStream inputStream = storage.get(TEST_ID);
    assertNotNull(inputStream);

    String downloadedText = new String(inputStream.readAllBytes());
    assertEquals(TEST_TEXT, downloadedText);
    System.out.println("S3 객체 다운로드 성공: " + downloadedText);
  }

  @Test
  @Order(3)
  void downloadTest() {
    BinaryContentDto binaryContentDto = new BinaryContentDto(
        TEST_ID,
        "test",
        1024L,
        "text/plain"
    );

    ResponseEntity<Void> response = storage.download(binaryContentDto);
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertNotNull(response.getHeaders().getLocation());
    System.out.println("Presigned URL 리다이렉트 생성 성공(URL: : " + response.getHeaders().getLocation());
  }
}
