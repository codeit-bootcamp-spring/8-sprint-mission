package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
  • findAllByUserId                  : 특정 유저의 모든 읽음 상태 조회
  • findAllByChannelId               : 특정 채널의 모든 읽음 상태 조회
  • findByUserIdAndChannelId         : 유저와 채널 조합으로 특정 읽음 상태 조회 (복합키 느낌)
  • deleteAllByChannelId             : 특정 채널 삭제될 때 해당 채널의 읽음 상태 일괄 삭제
  • findUsersByChannelId             : 채널의 ReadStatus와 연결된 User만 중복 제거 후 조회
 */
public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  @EntityGraph(attributePaths = {"user", "channel"})
  List<ReadStatus> findAllByUser_Id(UUID userId);

  List<ReadStatus> findAllByChannel_Id(UUID channelId);

  Optional<ReadStatus> findByUser_IdAndChannel_Id(UUID userId, UUID channelId);

  void deleteAllByChannel_Id(UUID channelId);

  @Query("SELECT DISTINCT rs.user FROM ReadStatus rs WHERE rs.channel.id = :channelId")
  List<User> findUsersByChannelId(@Param("channelId") UUID channelId);
}
