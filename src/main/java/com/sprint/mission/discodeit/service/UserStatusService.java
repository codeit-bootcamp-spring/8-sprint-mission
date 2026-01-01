package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/*
    UserStatusService
    -------------------------
    UserStatus(유저의 마지막 접속 시각, 온라인 상태)를 관리하는 서비스
 */
public interface UserStatusService {

    UserStatusResponse create(UserStatusCreateRequest request);

    UserStatusResponse find(UUID id);

    List<UserStatusResponse> findAll();

    UserStatusResponse update(UserStatusUpdateRequest request);

    UserStatusResponse updateByUserId(UUID userId, Instant lastConnAt);

    void delete(UUID id);
}
