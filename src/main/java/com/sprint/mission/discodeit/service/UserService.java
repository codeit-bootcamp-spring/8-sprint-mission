package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.response.UserResponse;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserResponse create(UserCreateRequest request, Optional<BinaryContentCreateRequest> requestDTO);
    // 단건 조회
    UserResponse findById(UUID id);

    //다건 조회
    //- 전체 조회
    List<UserResponse> findAll();

    //- 학년별 조회
    Map<Integer, List<User>> findUserByGrade();

    UserResponse update(UUID id, UserUpdateRequest request, Optional<BinaryContentCreateRequest> requestDTO);

    UserResponse updateOnlineStatus(UUID userId);

    UserResponse updateOfflineStatus(UUID userId);

    //유저 삭제
    void delete(UUID id);
}
