package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  // 특정 채널에 속한 모든 메시지 목록 반환
  List<Message> findAllByChannel_Id(UUID channelId);

  Optional<Message> findFirstByChannel_IdOrderByCreatedAtDesc(UUID channelId);
}
