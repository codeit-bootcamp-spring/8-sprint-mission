package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public UserDto updateRole(RoleUpdateRequest request) {

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다."));
    user.updateRole(request.newRole());

    return userMapper.toDto(user);
  }
}
