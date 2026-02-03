package com.sprint.mission.discodeit.storage.local;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type",
        havingValue = "local",
        matchIfMissing = false
)
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath) {
        this.root = Paths.get(rootPath);
    }

    @PostConstruct
    public void init() {
        try {
            // 루트 디렉토리가 존재하지 않으면 생성
            if (!Files.exists(root)) {
                Files.createDirectories(root);
                log.info("BinaryContent 저장 디렉토리 생성: {}", root.toAbsolutePath());
            } else {
                log.info("BinaryContent 저장 디렉토리 확인: {}", root.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("BinaryContent 저장 디렉토리 초기화 실패: {}", root.toAbsolutePath(), e);
            throw new RuntimeException("BinaryContent 저장 디렉토리 초기화 실패", e);
        }
    }

    /**
     * 파일의 실제 저장 위치에 대한 규칙을 정의합니다.
     * 파일 저장 위치 규칙: {root}/{UUID}
     */
    private Path resolvePath(UUID id) {
        return root.resolve(id.toString());
    }

    @Override
    public UUID put(UUID id, byte[] bytes) {
        try {
            Path filePath = resolvePath(id);
            // 파일이 이미 존재하면 덮어쓰기
            Files.write(filePath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            log.debug("BinaryContent 저장 완료: {}", filePath);
            return id;
        } catch (IOException e) {
            log.error("BinaryContent 저장 실패: id={}", id, e);
            throw new RuntimeException("BinaryContent 저장 실패: " + id, e);
        }
    }

    @Override
    public InputStream get(UUID id) {
        try {
            Path filePath = resolvePath(id);
            if (!Files.exists(filePath)) {
                log.warn("BinaryContent 파일을 찾을 수 없음: {}", filePath);
                return null;
            }
            return Files.newInputStream(filePath, StandardOpenOption.READ);
        } catch (IOException e) {
            log.error("BinaryContent 조회 실패: id={}", id, e);
            throw new RuntimeException("BinaryContent 조회 실패: " + id, e);
        }
    }

    @Override
    public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
        if (binaryContentDto == null || binaryContentDto.getId() == null) {
            throw new IllegalArgumentException("BinaryContentDto가 유효하지 않습니다.");
        }

        UUID id = binaryContentDto.getId();
        Path filePath = resolvePath(id);

        try {
            // 파일 존재 여부 확인
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + id);
            }

            // Resource 생성
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("파일을 읽을 수 없습니다: " + id);
            }

            // Content-Type 결정
            String contentType = binaryContentDto.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            // 파일명 결정
            String fileName = binaryContentDto.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                fileName = id.toString();
            }

            // ResponseEntity 생성
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("파일 다운로드 실패: id={}", id, e);
            throw new RuntimeException("파일 다운로드 실패: " + id, e);
        }
    }
}
