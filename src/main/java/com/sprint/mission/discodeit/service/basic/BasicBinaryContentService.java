package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    public BinaryContent create(BinaryContentCreateRequest request) {
        // 1. BinaryContent 엔티티 생성 (메타 정보만 저장)
        BinaryContent binaryContent = new BinaryContent(
                request.getFileName(),
                request.getContentType(),
                request.getFileSize()
        );
        
        // 2. 메타 정보를 데이터베이스에 저장 (ID 생성)
        BinaryContent savedContent = binaryContentRepository.save(binaryContent);
        
        // 3. 바이너리 데이터를 별도 저장소에 저장
        if (request.getBytes() != null && !request.getBytes().isEmpty()) {
            byte[] bytes = Base64.getDecoder().decode(request.getBytes());
            binaryContentStorage.put(savedContent.getId(), bytes);
        }
        
        return savedContent;
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
        // 1. 메타 정보 삭제
        binaryContentRepository.delete(id);
        // 2. 바이너리 데이터 삭제는 BinaryContentStorage 구현체에서 처리
        // (일부 구현체는 자동으로 처리할 수 있음)
    }
}