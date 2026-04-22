package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
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

  @Override
  @Transactional
  public BinaryContentDto create(BinaryContentCreateRequest request) {

    log.info("[BINARY_CONTENT] create start fileName={}", request.fileName());

    // 메타데이터
    BinaryContent binaryContent = new BinaryContent(
        request.fileName(),
        request.size(),
        request.contentType()
    );

    // 메타데이터 DB에 저장 (상태 PROCESSING)
    BinaryContent savedContent = binaryContentRepository.save(binaryContent);

    // 메타 데이터 저장 완료되면 이제 진짜 파일을 저장할 준비가 됐다는 이벤트 발행
    // create() 메서드가 종료되면서 DB 트랜잭션이 최종 반영된다.
    // 그때서야 리스너가 움직인다.
    eventPublisher.publishEvent(new BinaryContentCreatedEvent(savedContent.getId(), request.bytes()));

    log.info("[BINARY_CONTENT] create success binaryContentId={}", savedContent.getId());

    return binaryContentMapper.toDto(savedContent);
  }

  @Override
  public BinaryContentDto findById(UUID id) {
    return binaryContentRepository.findById(id)
        .map(binaryContentMapper::toDto)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids).stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!binaryContentRepository.existsById(id)) {
      throw new BinaryContentNotFoundException(id);
    }
    binaryContentRepository.deleteById(id);
  }

  // 업로드 완료되면 그 결과를 반영(업데이트)하는 메서드
  // 이미 메타데이터가 DB에 담기고, 메인 트랜잭션이 종료 되었으면
  // 다시 DB 수정이 안되기에 새로운 트랜잭션을 하나 더 만들어서 기록하는 것이다.
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateStatus(UUID id, BinaryContentStatus status) {
    BinaryContent content = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(id));

    content.updateStatus(status);

    log.info("[BINARY_CONTENT] status updated id={}, status={}", id, status);
  }
}
