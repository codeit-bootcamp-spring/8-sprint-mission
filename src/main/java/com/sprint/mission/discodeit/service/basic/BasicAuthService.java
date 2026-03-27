/*
package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserInvalidLoginException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto login(LoginRequest loginRequest) {
    log.info("[AuthService] 로그인 시작 - 이름: {}", loginRequest.username());

    String username = loginRequest.username();
    String password = loginRequest.password();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> {
          log.warn("[AuthService] 로그인 실패 - 존재하지 않는 사용자: {}", username);
          return new UserNotFoundException(username);
        });

    if (!user.getPassword().equals(password)) {
      log.warn("[AuthService] 로그인 실패 - 이름: {}", username);
      throw new UserInvalidLoginException();
    }

    log.info("[AuthService] 로그인 완료 - 이름: {}", username);
    return userMapper.toDto(user);
  }
}
*/
