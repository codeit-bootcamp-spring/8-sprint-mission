package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID>, MessageRepositoryCustom {

    Slice<Message> findAllByChannelIdWithCursor(UUID channelId, Instant cursor, Pageable pageable);

    Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId);

    void deleteAllByAuthorId(UUID authorId);

    void deleteAllByChannelId(UUID channelId);
}
