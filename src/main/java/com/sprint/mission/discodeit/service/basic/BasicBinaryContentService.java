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
        // BinaryContentCreateRequest의 fileType을 contentType으로 사용
        BinaryContent binaryContent = new BinaryContent(
                request.getFileName(),
                request.getFileType(), // MIME 타입
                request.getFileSize(),
                request.getBytes() // Base64 인코딩된 바이너리 데이터
        );
        return binaryContentRepository.save(binaryContent);
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return binaryContentRepository.findById(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        Object result = binaryContentRepository.findAll();
        if (result == null) {
            return List.of();
        }
        List<BinaryContent> allContents = (List<BinaryContent>) result;
        return allContents.stream()
                .filter(content -> ids.contains(content.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.delete(id);
    }
}