package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContent create(BinaryContentCreateRequest request) {
        // BinaryContent 생성자는 Base64 문자열을 받아서 byte 배열로 변환합니다
        BinaryContent binaryContent = new BinaryContent(
                request.getFileName(),
                request.getContentType(),
                request.getFileSize(),
                request.getBytes() != null ? request.getBytes() : ""
        );
        return binaryContentRepository.save(binaryContent);
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return binaryContentRepository.findById(id);
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        java.util.List<BinaryContent> allContents = binaryContentRepository.findAll();
        return allContents.stream()
                .filter(content -> ids.contains(content.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.delete(id);
    }
}