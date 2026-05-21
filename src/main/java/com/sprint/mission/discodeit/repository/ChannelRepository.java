package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

  List<Channel> findAllByTypeOrIdIn(ChannelType type, List<UUID> ids);

  @Query("select rs.user.id from ReadStatus rs where rs.channel.id = :channelId")
  List<UUID> findAllUserIdsByChannelId(UUID channelId);
}
