package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.DTO.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFReadStatusRepository implements ReadStatusRepository {
    private final Map<UUID, ReadStatus> data;

    public JCFReadStatusRepository() {
        /*
        //기존에 사용했던 HashMap은 모든 HTTP 요청이 같은 HashMap에 접근하기 때문에 Tread-Safe 하지 못함
        //기본적으로 @Repository scope는 Singleton
        //Tread-Safe한 ConcurrentHashMap사용
        */
        this.data = new ConcurrentHashMap<>();
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
    public Optional<ReadStatus> findByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .findFirst();
    }

    @Override
    public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId))
                .findFirst();
    }

    @Override
    public boolean existsByUserIdAndChannelId(UUID userId, UUID channelId) {
        return data.values().stream()
                .anyMatch(readStatus ->
                                readStatus.getUserId().equals(userId) &&
                                readStatus.getChannelId().equals(channelId)
                        );
    }

    @Override
    public List<UUID> findAllByChannelId(UUID channelId) {
        return data.values().stream()
                .map(ReadStatus::getChannelId)
                .filter(readStatus -> readStatus.equals(channelId))
                .toList();
    }

    @Override
    public void delete(UUID id) {
        this.data.remove(id);
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        data.values().removeIf(content -> content.getId().equals(channelId));
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .toList();
    }
}
