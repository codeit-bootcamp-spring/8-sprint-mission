package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binary.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binary.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.enums.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.binary.BinaryContentException;
import com.sprint.mission.discodeit.exception.enums.BinaryContentErrorCode;
import com.sprint.mission.discodeit.exception.enums.CommonErrorCode;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  // 이벤트 발행
  private final ApplicationEventPublisher eventPublisher;

  private final SseService sseService;
  private final BinaryContentMapper binaryContentMapper;

  @Override
  @Transactional
  public BinaryContent create(BinaryContentCreateRequest request) {
    validateCreateRequest(request);
    log.info("바이너리 컨텐츠 생성 요청: fileName={}, size={}", request.fileName(), request.size());

    // 1. 메타 데이터 저장용 엔티티 생성
    BinaryContent content = new BinaryContent(
        request.contentType(),
        request.fileName(),
        request.size()
    );
    BinaryContent saved = binaryContentRepository.save(content);

    // 이벤트 발행 로직
    eventPublisher.publishEvent(new BinaryContentCreatedEvent(saved.getId(), request.bytes()));

    log.info("바이너리 컨텐츠 메타데이터 생성 및 업로드 이벤트 발행 완료: id={}", saved.getId());
    return saved;
  }

  @Override
  public BinaryContent findById(UUID binaryContentId) {
    if (binaryContentId == null) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "binaryContentId는 필수입니다.");
    }
    log.debug("바이너리 컨텐츠 조회 요청: id={}", binaryContentId);

    return binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new BinaryContentException(BinaryContentErrorCode.FILE_NOT_FOUND));
  }

  @Override
  public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "ids는 필수이며 비어 있을 수 없습니다.");
    }
    log.debug("바이너리 컨텐츠 목록 조회 요청: ids={}", ids);

    return binaryContentRepository.findAllByIdIn(ids);
  }

  @Override
  @Transactional
  public void deleteById(UUID binaryContentId) {
    if (binaryContentId == null) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "binaryContentId는 필수입니다.");
    }
    log.info("바이너리 컨텐츠 삭제 요청: id={}", binaryContentId);

    binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new BinaryContentException(BinaryContentErrorCode.FILE_NOT_FOUND));

    binaryContentRepository.deleteById(binaryContentId);
  }

  // 리스너의 파일 업로드 성공/실패 여부로 상태를 변경하는 메서드
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BinaryContent updateStatus(UUID binaryContentId, BinaryContentStatus status) {
    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new BinaryContentException(BinaryContentErrorCode.FILE_NOT_FOUND));

    binaryContent.updateStatus(status);
    log.info("바이너리 컨텐츠 상태 업데이트 완료: id={}, status={}", binaryContentId, status);

    // SSE를 통해 클라이언트에 파일 업로드 상태 변경 이벤트 전송
    BinaryContentResponse binaryContentResponse = binaryContentMapper.toBinaryContentResponse(binaryContent);
    sseService.broadcast("binaryContents.updated", binaryContentResponse); // 모든 클라이언트에게 전송

    return binaryContent;
  }

  private void validateCreateRequest(BinaryContentCreateRequest request) {
    if (request == null) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "요청이 null입니다.");
    }
    if (request.bytes() == null || request.bytes().length == 0) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "bytes는 필수입니다.");
    }
    if (request.contentType() == null || request.contentType().isBlank()) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "contentType은 필수입니다.");
    }
    if (request.fileName() == null || request.fileName().isBlank()) {
      throw new DiscodeitException(CommonErrorCode.INVALID_INPUT_VALUE, "fileName은 필수입니다.");
    }
  }
}
