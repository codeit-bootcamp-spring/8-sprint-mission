package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.DomainEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
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
  private final JwtRegistry jwtRegistry;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails) {

    if (userDetails == null) {
      return null;
    }

    UUID userId = userDetails.getUserDto().id();

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    return userMapper.toDto(user);
  }

  @CacheEvict(value = "user", allEntries = true)
  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateUserRole(UserRoleUpdateRequest userRoleUpdateRequest) {

    User user = userRepository.findById(userRoleUpdateRequest.userId())
        .orElseThrow(() -> new UserNotFoundException(userRoleUpdateRequest.userId()));
    Role oldRole = user.getRole();

    user.updateRole(userRoleUpdateRequest.newRole());
    User updatedUser = userRepository.save(user);
    UserDto updatedUserDto = userMapper.toDto(updatedUser);

    jwtRegistry.invalidateJwtInformationByUserId(user.getId());

    RoleUpdatedEvent event = RoleUpdatedEvent.now(
        updatedUser.getId(),
        updatedUser.getUsername(),
        oldRole.name(),
        userRoleUpdateRequest.newRole().name()
    );

    eventPublisher.publishEvent(event);

    eventPublisher.publishEvent(new DomainEvent<>("users.updated", updatedUserDto, null));

    return updatedUserDto;
  }
}
