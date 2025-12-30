package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;

/*
    AuthService
    -------------------------
    로그인을 담당하는 서비스

    UserDto로 응답용 데이터를 반환하고,
    AuthLoginRequest 를 파라미터로 받아와서 로그인 일치 여부를 판단 한다. (newUsername, newPassword)
 */
public interface AuthService {

  UserResponse login(LoginRequest request);
}
