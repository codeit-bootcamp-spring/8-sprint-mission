package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final List<ReadStatus> list = new ArrayList<>();

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        list.removeIf(e -> e.getId().equals(readStatus.getId()));
        list.add(readStatus);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return list.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(list);
    }

    @Override
    public void delete(UUID id) {
        list.removeIf(e -> e.getId().equals(id));
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return list.stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return list.stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }
}