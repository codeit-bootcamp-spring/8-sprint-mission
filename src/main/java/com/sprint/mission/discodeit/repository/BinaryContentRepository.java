package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {

    BinaryContent save(BinaryContent binaryContent);

    // 이미지 단건 조회 (파일 하나만 필요할 때)
    Optional<BinaryContent> findById(UUID id);

    List<BinaryContent> findAllByIdIn(List<UUID> ids);

    void deleteById(UUID id);

    void deleteAllByIdIn(List<UUID> ids);


}
