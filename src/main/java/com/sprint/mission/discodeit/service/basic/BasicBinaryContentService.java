package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.base.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Override
  public BinaryContentDto create(BinaryContentCreateRequest binaryContentCreateRequest) {
    log.info("[BinaryContentService] 파일 저장 시작 - 이름: {}, 크기: {}",
        binaryContentCreateRequest.fileName(), binaryContentCreateRequest.bytes().length);

    String fileName = binaryContentCreateRequest.fileName();
    byte[] bytes = binaryContentCreateRequest.bytes();
    String contentType = binaryContentCreateRequest.contentType();
    BinaryContent binaryContent = new BinaryContent(
        fileName,
        (long) bytes.length,
        contentType
    );

    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
    log.debug("[BinaryContentService] DB 저장 완료 - ID: {}", binaryContent.getId());

    BinaryContentCreatedEvent event = BinaryContentCreatedEvent.now(
        savedBinaryContent.getId(),
        savedBinaryContent.getFileName(),
        bytes
    );

    eventPublisher.publishEvent(event);

    log.debug("[BinaryContentService] 파일 저장 이벤트 발행 완료 - Id: {}", savedBinaryContent.getId());
    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public BinaryContentDto find(UUID binaryContentId) {
    log.debug("[BinaryContentService] 파일 조회 시작 - Id: {}", binaryContentId);

    return binaryContentRepository.findById(binaryContentId)
        .map(binaryContentMapper::toDto)
        .orElseThrow(
            () -> {
              log.warn("[BinaryContentService] 파일 조회 실패 - 존재하지 않는 Id: {}", binaryContentId);
              return new BinaryContentNotFoundException(binaryContentId);
            });
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> binaryContentIds) {
    log.debug("[BinaryContentService] 파일 목록 조회 시작 - 요청 개수: {}개", binaryContentIds.size());

    List<BinaryContentDto> result = binaryContentRepository.findAllById(binaryContentIds).stream()
        .map(binaryContentMapper::toDto)
        .toList();

    log.info("[BinaryContentService] 파일 목록 조회 완료 - 결과 개수: {}개", result.size());
    return result;
  }

  @Transactional
  @Override
  public void delete(UUID binaryContentId) {
    log.info("[BinaryContentService] 파일 삭제 시작 - Id: {}", binaryContentId);

    if (!binaryContentRepository.existsById(binaryContentId)) {
      log.warn("[BinaryContentService] 존재하지 않는 Id: {}", binaryContentId);
      throw new BinaryContentNotFoundException(binaryContentId);
    }

    binaryContentRepository.deleteById(binaryContentId);
    log.info("[BinaryContentService] 파일 삭제 완료 - Id: {}", binaryContentId);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status) {
    log.info("[BinaryContentService] 파일 상태 업데이트 시작 - Id: {}", binaryContentId);

    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

    binaryContent.updateStatus(status);

    log.info("[BinaryContentService] 파일 상태 업데이트 완료 - Id: {}, status: {}", binaryContentId, status);
    return binaryContentMapper.toDto(binaryContent);
  }
}
