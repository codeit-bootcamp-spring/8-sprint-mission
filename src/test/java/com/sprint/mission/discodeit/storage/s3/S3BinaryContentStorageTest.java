package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BinaryContentStorage 통합 테스트")
public class S3BinaryContentStorageTest {

  @Autowired
  private S3BinaryContentStorage s3BinaryContentStorage;

  UUID uuid = UUID.randomUUID();
  byte[] bytes = "hello".getBytes();

  @Test
  @DisplayName("파일 업로드 성공")
  void putBinaryContent_Success() {

    // when
    UUID result = s3BinaryContentStorage.put(uuid, bytes);

    // then
    assertThat(result).isEqualTo(uuid);
  }

  @Test
  @DisplayName("파일 조회 성공")
  void getBinaryContent_Success() throws IOException {

    // given
    s3BinaryContentStorage.put(uuid, bytes);

    // when
    InputStream result = s3BinaryContentStorage.get(uuid);

    // then
    assertThat(result).isNotNull();

    assertThat(result.readAllBytes()).isEqualTo(bytes);
  }

  @Test
  @DisplayName("파일 다운로드 성공")
  void downloadBinaryContent_Success() {

    // given
    s3BinaryContentStorage.put(uuid, bytes);

    BinaryContentDto binaryContentDto = new BinaryContentDto(uuid, "hello.txt", 10L, "text/plain");

    // when
    ResponseEntity<?> response = s3BinaryContentStorage.download(binaryContentDto);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(response.getHeaders().getLocation()).isNotNull();
  }

  @Test
  @DisplayName("Presigned URL 생성 성공")
  void generatePresignedUrl_Success() {

    // given
    BinaryContentDto binaryContentDto = new BinaryContentDto(uuid, "hello.txt", 10L, "text/plain");
    ResponseEntity<?> response = s3BinaryContentStorage.download(binaryContentDto);

    // when
    String url = response.getHeaders().getLocation().toString();

    // then
    assertThat(url).isNotNull();
    assertThat(url).contains("amazonaws.com");
  }
}
