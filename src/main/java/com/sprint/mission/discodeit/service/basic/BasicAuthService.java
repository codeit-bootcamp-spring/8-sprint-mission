package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateUserRole(UserRoleUpdateRequest userRoleUpdateRequest) {

    User user = userRepository.findById(userRoleUpdateRequest.userId())
        .orElseThrow(() -> new UserNotFoundException(userRoleUpdateRequest.userId()));

    user.updateRole(userRoleUpdateRequest.newRole());

    return userMapper.toDto(user);
  }
}
