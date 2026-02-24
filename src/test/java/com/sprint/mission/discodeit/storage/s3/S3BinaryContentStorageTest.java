package com.sprint.mission.discodeit.storage.s3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class S3BinaryContentStorageTest {

  @Autowired
  private S3BinaryContentStorage s3Storage;

  @Autowired
  private S3Properties s3Properties;

  @Test
  @DisplayName("파일 업로드(put) 및 다운로드 통합 테스트")
  void putAndGetTest() throws IOException {

    // 입력값 설정
    UUID fileId = UUID.randomUUID();

    String content = "Jun's test file contents";

    byte[] data = content.getBytes();

    // 업로드
    s3Storage.put(fileId, data);

    // 검증
    try (InputStream is = s3Storage.get(fileId)) {
      assertNotNull(is);
      byte[] downloadData = is.readAllBytes();

      // 보낸 데이터, 받은 데이터 같은지 비교
      assertArrayEquals(data, downloadData);
    }
  }

  @Test
  @DisplayName("Presigned URL 리다이렉트 응답 테스트")
  void downloadRedirectTest() {

    UUID fileId = UUID.randomUUID();

    String content = "Jun's Redirect 테스트 file contents";

    byte[] data = content.getBytes(StandardCharsets.UTF_8);

    s3Storage.put(fileId, data);

    BinaryContentDto dto = new BinaryContentDto(
        fileId,
        "jun-redirect-file.txt",
        (long) data.length,
        "text/plain; charset=utf-8"
    );

    // download 호출
    ResponseEntity<?> download = s3Storage.download(dto);

    assertEquals(HttpStatus.SEE_OTHER, download.getStatusCode());

    String location = download.getHeaders().getLocation().toString();
    assertNotNull(location);
    assertTrue(location.contains(s3Properties.bucket())); // S3 버킷 명 포함 여부 확인
    assertTrue(location.contains(fileId.toString()));     // 파일 식별자(ID) 포함 여부 확인
  }

}
