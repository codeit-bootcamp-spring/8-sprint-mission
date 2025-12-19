package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;

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
    UserDto create(UserCreateRequest request);
    
    // 유저 조회 (단건)
    UserDto findUser(UUID id);

    // 유저 조회 (다건)
    List<UserDto> findAll();

    // 수정
    UserDto update(UserUpdateRequest request);
    
    // 삭제
    void delete(UUID id);

    
}
