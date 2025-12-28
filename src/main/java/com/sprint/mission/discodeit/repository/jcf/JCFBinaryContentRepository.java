package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JCFBinaryContentRepository implements BinaryContentRepository {
    // 메모리에 BinaryContent를 저장할 Map
    private final Map<UUID, BinaryContent> database = new HashMap<>();

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        database.put(binaryContent.getId(), binaryContent); //
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(database.get(id)); //
    }

    @Override
    public Object findAll() {
    }

    @Override
    public void delete(UUID id) {
    }
}