package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
@Component
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") Path root
  ) {
    this.root = root;
  }

  @PostConstruct
  public void init() {
    try {
      if (!Files.exists(root)) {
        Files.createDirectories(root);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  Path resolvePath(UUID binaryContentId) {
    return root.resolve(binaryContentId.toString());
  }

  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    Path path = resolvePath(binaryContentId);

    if (Files.exists(path)) {
      throw new IllegalArgumentException("해당 파일이 이미 존재합니다.");
    }

    try {
      Files.write(path, bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return binaryContentId;
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    Path path = resolvePath(binaryContentId);

    if (!Files.exists(path)) {
      throw new IllegalArgumentException("파일이 존재하지 않습니다.");
    }

    try {
      return new FileInputStream(path.toFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto file) {
    InputStream inputStream = get(file.id());
    Resource resource = new InputStreamResource(inputStream);

    return ResponseEntity
        .status(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.fileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, file.contentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.size()))
        .body(resource);
  }
}