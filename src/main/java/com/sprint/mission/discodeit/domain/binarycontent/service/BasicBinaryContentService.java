package com.sprint.mission.discodeit.domain.binarycontent.service;

import com.sprint.mission.discodeit.domain.binarycontent.domain.BinaryContent;
import com.sprint.mission.discodeit.domain.binarycontent.domain.BinaryContentStatus;
import com.sprint.mission.discodeit.domain.binarycontent.dto.domain.BinaryContentDto;
import com.sprint.mission.discodeit.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.domain.binarycontent.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.domain.binarycontent.event.BinaryContentStatusUpdatedEvent;
import com.sprint.mission.discodeit.domain.binarycontent.exception.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.domain.binarycontent.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.domain.binarycontent.repository.BinaryContentRepository;
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
public class BasicBinaryContentService implements BinaryContentService
{
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public BinaryContentDto create(BinaryContentCreateRequest binaryContentCreateRequest) {
        log.debug("바이너리 컨텐츠 생성 처리 시작: fileName={}", binaryContentCreateRequest.fileName());

        BinaryContent binaryContent = new BinaryContent(
                binaryContentCreateRequest.fileName(),
                (long) binaryContentCreateRequest.bytes().length,
                binaryContentCreateRequest.contentType(),
                BinaryContentStatus.PROCESSING
        );

        BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

        eventPublisher.publishEvent(new BinaryContentCreatedEvent(savedBinaryContent.getId(), binaryContentCreateRequest.bytes()));

        log.info("바이너리 컨텐츠 생성 처리 완료: binaryContentId={}", savedBinaryContent.getId());

        return binaryContentMapper.toDto(savedBinaryContent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateStatus(UUID binaryContentId, BinaryContentStatus binaryContentStatus) {
        BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

        binaryContent.updateStatus(binaryContentStatus);

        BinaryContentDto binaryContentDto = binaryContentMapper.toDto(binaryContent);
        eventPublisher.publishEvent(new BinaryContentStatusUpdatedEvent("binaryContents.updated", binaryContentDto, binaryContent.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    @Override
    public BinaryContentDto find(UUID binaryContentId) {
        return binaryContentRepository.findById(binaryContentId)
                .map(binaryContent -> binaryContentMapper.toDto(binaryContent))
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BinaryContentDto> findAllByIds(List<UUID> binaryContentIds) {
        return binaryContentRepository.findAllById(binaryContentIds).stream()
                .map(binaryContent -> binaryContentMapper.toDto(binaryContent))
                .toList();
    }

    @Transactional
    @Override
    public void delete(UUID binaryContentId) {
        BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new BinaryContentNotFoundException(binaryContentId));

        binaryContentRepository.delete(binaryContent);
    }
}
