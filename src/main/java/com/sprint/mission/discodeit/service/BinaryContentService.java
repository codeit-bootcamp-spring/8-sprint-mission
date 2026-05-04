package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentDto create(BinaryContentCreateRequest request);

    BinaryContentDto find(UUID id);

    List<BinaryContentDto> findAllByIn(List<UUID> binaryContentIds);

    void delete(UUID binaryContentId);

    BinaryContentDto updateStatus(UUID binaryContentId, BinaryContentStatus status);
}
