package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
import org.springframework.web.util.UriUtils;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "discodeit.storage",
    name = "type",
    havingValue = "local"
)
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") String rootPath
  ) {
    this.root = Paths.get(rootPath);
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(root);
      log.info("LocalBinaryContentStorage 초기화 작업 root = {}", root.toAbsolutePath());
    } catch (IOException e) {
      throw new RuntimeException("LocalBinaryContentStorage 루트 디렉터리 생성 실패: " + root, e);
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    if (id == null) {
      throw new IllegalArgumentException("id 값이 null 입니다.");
    }
    if (bytes == null) {
      throw new IllegalArgumentException("bytes 값이 null 입니다.");
    }

    try {
      log.info("[SIMULATION] 3초 지연 발생 시작");
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

    Path path = resolvePath(id);

    try {
      Files.write(path, bytes, StandardOpenOption.CREATE);
      return id;
    } catch (IOException e) {
      throw new IllegalArgumentException("파일 저장 실패: " + path, e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("id 값이 null 입니다.");
    }

    Path path = resolvePath(id);

    try {
      return Files.newInputStream(path, StandardOpenOption.READ);
    } catch (NoSuchFileException e) {
      throw new IllegalStateException("파일이 존재하지 않습니다.: " + path, e);
    } catch (IOException e) {
      throw new IllegalStateException("파일 읽기 실패: " + path, e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto dto) {
    if (dto == null) {
      throw new IllegalArgumentException("dto가 null 입니다.");
    }

    InputStream in = get(dto.id());

    Resource resource = new InputStreamResource(in);

    String encodedFileName = UriUtils.encode(dto.fileName(), StandardCharsets.UTF_8);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(dto.contentType()));
    headers.setContentDisposition(
        ContentDisposition.attachment()
            .filename(encodedFileName, StandardCharsets.UTF_8)
            .build()
    );

    if (dto.size() != null) {
      headers.setContentLength(dto.size());
    }

    return ResponseEntity.ok()
        .headers(headers)
        .body(resource);
  }
}
