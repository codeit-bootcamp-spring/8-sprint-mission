package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse create(UserCreateRequest request);

    UserResponse update(UserUpdateRequest request);

    //  유저 생성 (profileImage 포함)
    UserResponse create(String name, String email, String password, String profileImage);

    //  로그인 및 비밀번호 검증
    UserResponse login(String email, String password);

    List<UserResponse> findAll();
    UserResponse findById(UUID id);

    //  유저 정보 수정 (프로필 이미지 반영)
    UserResponse update(UUID id, String name, String password, String profileImage);

    void delete(UUID id);
}