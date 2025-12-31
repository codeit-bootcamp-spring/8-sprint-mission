package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;

  @Override
  public UserResponse login(LoginRequest request) {

    // 보안상 아이디/비번 중 무엇이 틀렸는지에 대한 정보 X (추측 방지)
    User user = userRepository.findByUsername(request.username())
        .filter(u -> u.getPassword().equals(request.password()))
        .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

    // UserStatus 조회 및 DTO 변환
    return userStatusRepository.findByUserId(user.getId())
        .map(status -> UserResponse.of(user, status))
        .orElseGet(() -> UserResponse.of(user, null));
  }
}
