package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusService {
    //  추상 메서드 하나만 남깁니다.
    UserStatusResponse update(UserStatusRequest request);

    UserStatus create(UUID userId);
    Optional<UserStatus> findById(UUID id);
    List<UserStatus> findAll();
    Optional<UserStatus> findByUserId(UUID userId);
    void delete(UUID id);
}