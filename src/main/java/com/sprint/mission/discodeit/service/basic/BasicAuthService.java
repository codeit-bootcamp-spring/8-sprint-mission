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

    // 1) 유저 이름으로 유저 조회
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(
            () -> new NoSuchElementException(request.username() + "사용자는 존재하지 않는 사용자 입니다."));

    // 2) 유저 비밀번호로 검증
    if (!user.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
    }

    // 3) UserStatus 조회 (없을 수도 있기에 Optional)
    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
        .orElse(null);

    // 4) User + UserStatus를 UserDto로 변환해서 반환 해준다.
    return convertDto(user, userStatus);
  }

  /*
      User + UserStatus를 외부에 출력하기 위한 UserDto로 변환시킨다.
      Online과 lastConnAt은 UserStatus를 토대로 계산한다.
   */
  private UserResponse convertDto(User user, UserStatus userStatus) {
    boolean online = false;
    Instant lastConn = null;

    if (userStatus != null) {
      online = userStatus.isOnline();
      lastConn = userStatus.getLastActiveAt();
    }

    return new UserResponse(
        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getName(),
        user.getEmail(),
        online,
        lastConn,
        user.getProfileId()
    );
  }
}
