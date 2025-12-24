package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> data;

    public JCFReadStatusRepository() {
        data = new HashMap<>();
    }

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        data.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    @Override
    public Optional<ReadStatus> findByChannelId(UUID id) {
        return Optional.empty();
    }

    @Override
    public boolean existsByUserIdAndChannelId(UUID uId, UUID cId) {
        return data.values().stream()
                .anyMatch(readStatus ->
                                readStatus.getUId().equals(uId) &&
                                readStatus.getCId().equals(cId)
                        );
    }

    @Override
    public List<UUID> findAllByChannelId(UUID cId) {
        return data.values().stream()
                .map(ReadStatus::getCId)
                .filter(readStatus -> readStatus.equals(cId))
                .toList();
    }

//    @Override
//    public List<UUID> findChannelIdsByUserId(UUID userId) {
//        return data.values().stream()
//                .map(ReadStatus::getUId)
//                .filter(readStatus -> readStatus.equals(userId))
//                .toList();
//    }

    @Override
    public void delete(UUID id) {
        this.data.remove(id);
    }

    @Override
    public void deleteAllByChannelId(UUID cId) {
        data.values().removeIf(content -> content.getId().equals(cId));
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID uId) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getUId().equals(uId))
                .toList();
    }
}
