package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final List<BinaryContent> list = new ArrayList<>();

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        list.removeIf(e -> e.getId().equals(binaryContent.getId()));
        list.add(binaryContent);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return list.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(list);
    }

    @Override
    public void delete(UUID id) {
        list.removeIf(e -> e.getId().equals(id));
    }
}