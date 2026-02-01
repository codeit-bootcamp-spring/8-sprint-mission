package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") String path) {
    this.root = Paths.get(path).toAbsolutePath().normalize();
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(this.root);
      log.info("Local storage initialized at: {}", this.root);
    } catch (IOException e) {
      throw new RuntimeException("로컬 스토리지 디렉토리 생성 실패", e);
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    try {
      Path filePath = resolvePath(id);
      Files.write(filePath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      log.info("파일 저장 완료: {}", filePath);
      return id;
    } catch (IOException e) {
      log.error("파일 저장 중 오류 발생 id={}", id, e);
      throw new RuntimeException("파일 쓰기 실패", e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    try {
      Path filePath = resolvePath(id);

      if (!Files.exists(filePath)) {
        throw new FileNotFoundException(id + " 해당 파일이 존재하지 않습니다.");
      }
      return Files.newInputStream(filePath);
    } catch (IOException e) {
      throw new RuntimeException("파일 읽어오는 과정에서 에러가 발생했습니다.", e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
    InputStream inputStream = get(binaryContentDto.id());

    Resource resource = new InputStreamResource(inputStream);

    ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
        .filename(binaryContentDto.fileName(), StandardCharsets.UTF_8)
        .build();

    return ResponseEntity.ok()
        //다운로드 시 브라우저가 인식할 파일명 설정 (attachment: 다운로드, inline: 미리보기)
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        // 파일의 MIME 타입 설정 (예: image/png, application/pdf)
        .contentType(MediaType.parseMediaType(binaryContentDto.contentType()))
        // 파일 크기 설정 - 브라우저 다운로드 진행률 표시에 도움됨(남은 시간, 진행률 바 나옴!)
        .contentLength(binaryContentDto.size())
        .body(resource);
  }

  @Override
  public void delete(UUID id) {
    try {
      Path filePath = resolvePath(id);
      Files.deleteIfExists(filePath);
      log.info("파일 삭제 완료: {}", filePath);
    } catch (IOException e) {
      log.error("파일 삭제 실패 id={}", id, e);
      throw new RuntimeException(e);
    }
  }
}
