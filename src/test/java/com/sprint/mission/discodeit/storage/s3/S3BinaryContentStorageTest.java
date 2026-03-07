package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "discodeit.storage.type=s3")
@ActiveProfiles("test")
class S3BinaryContentStorageTest {

  @Autowired
  private S3BinaryContentStorage s3BinaryContentStorage;

  @Test
  @DisplayName("S3에 바이너리 데이터를 저장(put)하고 가져올(get) 수 있다")
  void putAndGetTest() throws IOException {
    // given
    UUID contentId = UUID.randomUUID();
    byte[] originalContent = "S3 테스트 데이터".getBytes();

    // when
    s3BinaryContentStorage.put(contentId, originalContent);
    InputStream inputStream = s3BinaryContentStorage.get(contentId);
    byte[] downloadedContent = inputStream.readAllBytes();

    // then
    assertThat(downloadedContent).isEqualTo(originalContent);
  }

  @Test
  @DisplayName("download 메서드 호출 시 302 리다이렉트와 Presigned URL이 반환된다")
  void downloadRedirectTest() {
    // given
    UUID contentId = UUID.randomUUID();

    BinaryContentDto metaData = new BinaryContentDto(
        contentId,
        "test.png",
        1024L,
        "image/png"
    );

    // when
    ResponseEntity<Void> response = s3BinaryContentStorage.download(metaData);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND); // 302 확인
    assertThat(response.getHeaders().getLocation()).isNotNull();

    String redirectUrl = response.getHeaders().getLocation().toString();
    assertThat(redirectUrl).contains("amazonaws.com"); // S3 주소 포함 확인
    assertThat(redirectUrl).contains("response-content-type=image%2Fpng"); // 컨텐츠 타입 포함 확인
  }
}