package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatusResponse create(UserStatusCreateRequest request);
    UserStatus find(UUID id);
    List<UserStatus> findAll();
    void updateByUserId(UUID uid);
    void delete(UUID id);
}
