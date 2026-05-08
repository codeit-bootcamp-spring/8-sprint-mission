package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.Sse.BinaryContent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.BinaryContent.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.exception.BinaryContentException.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        log.info("Service: 파일 업로드 요청 - 파일명: {}, 크기: {} bytes, 타입: {}",
                request.fileName(), request.bytes().length, request.contentType());
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                (long) request.bytes().length,
                request.contentType(),
                BinaryContentStatus.PROCESSING
        );

        BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

        createdEvent(savedBinaryContent.getId(), request.bytes());

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
            log.warn("Service: 파일 삭제 실패(존재하지 않는 바이너리 아이디) - ID: {}", binaryContentId);
            throw new BinaryContentNotFoundException(binaryContentId);
        }

        binaryContentRepository.deleteById(binaryContentId);
        log.info("Service: 파일 DB 삭제 완료 - ID: {}", binaryContentId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status) {
        log.info("Service: 바이너리 상태 변경 로직 실행 - ID: {}", binaryContentId);
        BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

        binaryContent.updateStatus(status);

        BinaryContentDto binaryContentDto = binaryContentMapper.toDto(binaryContent);

        eventPublisher.publishEvent(new BinaryContentUpdatedEvent(binaryContentDto, status));

        log.info("Service: 바이너리 상태 변경이 성공하였습니다. ID: {}, status: {}", binaryContentId, status);

        return binaryContentDto;
    }

    // 생성 이벤트 발행 중복 코드
    private void createdEvent(UUID id, byte[] bytes) {
        BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(
                id,
                bytes
        );
        eventPublisher.publishEvent(event);
    }
}
