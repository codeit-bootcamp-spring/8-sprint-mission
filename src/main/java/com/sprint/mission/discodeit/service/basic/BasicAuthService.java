package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto login(LoginRequest request) {
    String username = request.username();
    String password = request.password();

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NoSuchElementException("해당 유저가 존재하지 않습니다."));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException("패스워드가 틀렸습니다.");
    }

    return userMapper.toDto(user);
  }
}
