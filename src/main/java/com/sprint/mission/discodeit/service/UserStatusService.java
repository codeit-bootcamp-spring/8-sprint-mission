package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatus create(UserStatusCreateRequest request);

  UserStatus find(UUID id);

  List<UserStatus> findAll();

  UserStatus update(UUID userStatusId, UserStatusUpdateRequest request);

  UserStatus updateByUserId(UUID uid, UserStatusUpdateRequest request);

  void delete(UUID id);
}
