package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {
    UserStatus save(UserStatus userStatus);
    boolean  existsByUserId(UUID id);
    Optional<UserStatus> findById(UUID id);
    Optional<UserStatus> findByUserId(UUID uId);
    List<UserStatus> findAll();
    void delete(UUID id);
    void deleteByUserId(UUID id);

}
