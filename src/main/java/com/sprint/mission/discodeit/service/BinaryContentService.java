package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentService {
    // DTO를 활용해 파라미터를 그룹화하여 생성합니다.
    BinaryContent create(BinaryContentCreateRequest request);

    // ID로 조회합니다.
    Optional<BinaryContent> findById(UUID id);

    // ID 목록으로 여러 객체를 한 번에 조회합니다.
    List<BinaryContent> findAllByIdIn(List<UUID> ids);

    // ID로 삭제합니다.
    void delete(UUID id);
}