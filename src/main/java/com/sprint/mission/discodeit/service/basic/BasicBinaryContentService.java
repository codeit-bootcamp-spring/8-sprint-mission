package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.BinaryContentWithBytesDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  /** 파일 업로드 저장 (프로필/첨부 등). */
  @Transactional
  @Override
  public BinaryContentDto create(BinaryContentCreateRequest request) {
    String fileName = request.fileName();
    byte[] bytes = request.bytes();
    String contentType = request.contentType();
    log.debug("파일 업로드, fileName={}, size={}, contentType={}", fileName, bytes.length, contentType);
    BinaryContent binaryContent = new BinaryContent(
        fileName,
        (long) bytes.length,
        contentType
    );
    binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(binaryContent.getId(), bytes);
    log.info("파일 업로드 완료, binaryContentId={}, fileName={}, size={}",
        binaryContent.getId(), fileName, bytes.length);
    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public BinaryContentDto find(UUID binaryContentId) {
    return binaryContentRepository.findById(binaryContentId)
        .map(binaryContentMapper::toDto)
        .orElseThrow(() -> {
          log.warn("바이너리 조회 실패: 없음, binaryContentId={}", binaryContentId);
          return new NoSuchElementException(
              "BinaryContent with id " + binaryContentId + " not found");
        });
  }

  @Override
  public BinaryContentWithBytesDto findWithBytes(UUID binaryContentId) {
    BinaryContentDto dto = find(binaryContentId);
    try {
      byte[] bytes = binaryContentStorage.get(binaryContentId).readAllBytes();
      String base64 = Base64.getEncoder().encodeToString(bytes);
      return new BinaryContentWithBytesDto(
          dto.id(),
          dto.fileName(),
          dto.size(),
          dto.contentType(),
          base64
      );
    } catch (IOException e) {
      log.error("바이너리 읽기 실패, binaryContentId={}", binaryContentId, e);
      throw new RuntimeException("Failed to read binary content: " + binaryContentId, e);
    }
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds) {
    return binaryContentRepository.findAllById(binaryContentIds).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public void delete(UUID binaryContentId) {
    log.debug("바이너리 삭제 시도, binaryContentId={}", binaryContentId);
    if (!binaryContentRepository.existsById(binaryContentId)) {
      log.warn("바이너리 삭제 실패: 없음, binaryContentId={}", binaryContentId);
      throw new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found");
    }
    binaryContentRepository.deleteById(binaryContentId);
    log.info("바이너리 삭제 완료, binaryContentId={}", binaryContentId);
  }
}
