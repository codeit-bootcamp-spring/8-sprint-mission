package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.dto.UserStatusDto;
import com.sprint.mission.discodeit.DTO.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserStatusUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto create(UserStatusCreateRequest request);

  UserStatusDto find(UUID id);

  List<UserStatusDto> findAll();

  UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request);

  UserStatusDto updateByUserId(UUID uid, UserStatusUpdateRequest request);

  void delete(UUID id);
}
