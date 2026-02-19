package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BinaryContentException.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentMapper binaryContentMapper;

  @Override
  @Transactional
  public BinaryContentDto create(BinaryContentCreateRequest request) {
    log.info("Service: 파일 업로드 요청 - 파일명: {}, 크기: {} bytes, 타입: {}",
        request.fileName(), request.bytes().length, request.contentType());
    BinaryContent binaryContent = new BinaryContent(
        request.fileName(),
        (long) request.bytes().length,
        request.contentType()
    );

    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

    binaryContentStorage.put(savedBinaryContent.getId(), request.bytes());

    log.info("Service: 파일 업로드 완료 - ID: {}", savedBinaryContent.getId());

    return binaryContentMapper.toDto(savedBinaryContent);
  }

  @Override
  public BinaryContentDto find(UUID binaryContentId) {
    return binaryContentRepository.findById(binaryContentId)
        .map(binaryContentMapper::toDto)
        .orElseThrow(
            () -> {
              log.warn("Service: 파일 조회 실패(존재하지 않는 ID) ID: {}", binaryContentId);
              return new BinaryContentNotFoundException(binaryContentId);
            });

  }

  @Override
  public List<BinaryContentDto> findAllByIn(List<UUID> binaryContentIds) {
    return binaryContentRepository.findAllByIdIn(binaryContentIds).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID binaryContentId) {
    log.info("Service: 파일 삭제 요청 - ID: {}", binaryContentId);
    if (!binaryContentRepository.existsById(binaryContentId)) {
      log.warn("Service: 파일 삭제 실패(존재하지 않는 아이디) - ID: {}", binaryContentId);
      throw new BinaryContentNotFoundException(binaryContentId);
    }

    binaryContentRepository.deleteById(binaryContentId);
    log.info("Service: 파일 DB 삭제 완료 - ID: {}", binaryContentId);
  }
}
