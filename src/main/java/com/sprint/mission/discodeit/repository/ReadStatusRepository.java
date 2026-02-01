package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);

  //N+1  문제 해결
  //Channel - update에서 발생 가능성 있음
  //SELECT * FROM read_statuses where id = user_id_1 이런식으로 (N번 호출)
  @Query("""
                  SELECT rs 
                  FROM ReadStatus rs 
                    JOIN FETCH rs.user u
                    LEFT JOIN FETCH u.profile
                  WHERE rs.channel.id = :channelId
      """)
  List<ReadStatus> findAllByChannelIdWithUser(@Param("channelId") UUID channelId);

  // 채널, 유저 정보 즉시 로딩
  @Query(""" 
              SELECT rs 
              FROM ReadStatus rs
                JOIN FETCH rs.channel
                JOIN FETCH rs.user u
                LEFT JOIN FETCH u.profile
              WHERE rs.user.id = :userId
      """)
  List<ReadStatus> findAllByUserIdWithChannel(@Param("userId") UUID userId);
}
