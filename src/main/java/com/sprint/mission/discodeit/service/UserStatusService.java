package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusService {
    // ✅ 추상 메서드 하나만 남기고 중복된 default 메서드는 삭제하세요.
    UserStatusResponse update(UserStatusRequest request);

    UserStatus create(UUID userId);

    UserStatus create(UserStatusCreateRequest request);

    Optional<UserStatus> findById(UUID id);
    List<UserStatus> findAll();
    Optional<UserStatus> findByUserId(UUID userId);

    UserStatus update(UserStatusUpdateRequest request);

    UserStatus updateByUserId(UUID userId, boolean isOnline);

    void delete(UUID id);
}