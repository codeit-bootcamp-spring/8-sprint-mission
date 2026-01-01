package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {
    ReadStatus save(ReadStatus readStatus);
    Optional<ReadStatus> findById(UUID id);
    Optional<ReadStatus> findByChannelId(UUID channelId);
    Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId);
    boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);
    List<UUID> findAllByChannelId(UUID channelId);
    void delete(UUID id);
    void deleteAllByChannelId(UUID channelId);
    List<ReadStatus> findAllByUserId(UUID userId);
}
