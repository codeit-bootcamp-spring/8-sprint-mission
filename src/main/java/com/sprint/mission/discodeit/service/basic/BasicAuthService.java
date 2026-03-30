package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final SessionRegistry sessionRegistry;

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

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateUserRole(UserRoleUpdateRequest userRoleUpdateRequest) {

    User user = userRepository.findById(userRoleUpdateRequest.userId())
        .orElseThrow(() -> new UserNotFoundException(userRoleUpdateRequest.userId()));

    user.updateRole(userRoleUpdateRequest.newRole());

    invalidateUserSession(user.getUsername());

    return userMapper.toDto(user);
  }

  private void invalidateUserSession(String username) {
    try {
      log.info("[AuthService] 세션 무효화 시작 - 대상: {}", username);

      List<Object> principals = sessionRegistry.getAllPrincipals();

      for (Object principal : principals) {
        if (principal instanceof UserDetails userDetails) {
          String principalName = userDetails.getUsername();

          if (username.equals(principalName)) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation session : sessions) {
              log.info("[AuthService] 세션 무효화 수행 - 세션ID: {}", session.getSessionId());
              session.expireNow();
            }
            log.info("[AuthService] 사용자 '{}'의 세션 {}개 무효화 완료", username, sessions.size());
            break;
          }
        }
      }
    } catch (Exception e) {
      log.error("[AuthService] 세션 무효화 중 예상치 못한 오류 발생", e);
    }
  }
}
