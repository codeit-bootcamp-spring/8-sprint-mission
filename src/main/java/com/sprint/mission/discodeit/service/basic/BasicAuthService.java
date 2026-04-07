package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public UserDto updateRole(RoleUpdateRequest request) {
    return updateRoleInternal(request);
  }

  @Transactional
  @Override
  public UserDto updateRoleInternal(RoleUpdateRequest request) {
    UUID userId = request.userId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    Role newRole = request.newRole();
    user.updateRole(newRole);

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public JwtDto refresh(String refreshToken) {
    try {
      if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
        throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
      }
      String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

      DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(
          username);

      String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

      String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

      return new JwtDto(userDetails.getUserDto(), newAccessToken, newRefreshToken);

    } catch (Exception e) {
      throw new RuntimeException("토큰 재발급 중 오류가 발생했습니다: " + e.getMessage());
    }
  }
}
