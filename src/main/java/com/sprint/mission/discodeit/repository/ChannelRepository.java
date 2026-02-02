package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

  @Query("""
      select c
      from Channel c
      where c.type = com.sprint.mission.discodeit.entity.ChannelType.PUBLIC
         or exists (
              select 1
              from ReadStatus rs
              where rs.channel = c
                and rs.user.id = :userId
         )
      """)
  List<Channel> findAllAccessibleByUserId(@Param("userId") UUID userId);
}
