package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.entity.Role;

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

    // 유저 역할(권한) 변경
    UserDto updateUserRole(UUID userId, Role newRole);
}
