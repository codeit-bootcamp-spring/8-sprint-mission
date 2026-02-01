package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MessageRepositoryCustom {

  Slice<Message> findAllByChannelIdWithCursor(UUID channelId, Instant cursor, Pageable pageable);

  Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId);
}
