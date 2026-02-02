package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/*
    • findAllByUserId                  : 특정 유저의 ID를 통해 온라인 상태 정보 조회
 */
public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {

  Optional<UserStatus> findByUser_Id(UUID userId);
}
