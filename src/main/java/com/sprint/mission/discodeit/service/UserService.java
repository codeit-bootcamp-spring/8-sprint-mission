package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.UUID;


/*
    UserService
    -------------------------
    User 관련 비즈니스 기능을 정의하는 서비스 인터페이스.

    항상 DTO(UserDto, UserCreateRequest, UserUpdateRequest)를 통해서만
    데이터를 주고받는다.
 */
public interface UserService {

  // 유저 생성
  UserResponse create(UserCreateRequest request, BinaryContentCreateRequest profileRequest);

  // 유저 조회 (단건)
  UserResponse findUser(UUID id);

  // 유저 조회 (다건)
  List<UserResponse> findAll();

  // 수정
  UserResponse update(UUID userid, UserUpdateRequest request,
      BinaryContentCreateRequest profileRequest);

  // 삭제
  void delete(UUID id);


}
