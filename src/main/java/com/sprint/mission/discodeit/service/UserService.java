package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

  UserDto create(UserCreateRequest request,
      Optional<BinaryContentCreateRequest> profileCreateRequest);

  // 단건 조회
  UserDto find(UUID id);

  //다건 조회
  //- 전체 조회
  List<UserDto> findAll();

  UserDto update(UUID id, UserUpdateRequest request,
      Optional<BinaryContentCreateRequest> profileCreateRequest);

  //유저 삭제
  void delete(UUID id);
}
