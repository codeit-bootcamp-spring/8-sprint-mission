package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final SessionRegistry sessionRegistry;

  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public UserDto updateRole(RoleUpdateRequest request) {

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다."));

    user.updateRole(request.newRole());

    invalidateUserSessions(user.getId());

    return userMapper.toDto(user);
  }

  private void invalidateUserSessions(UUID userId) {
    sessionRegistry.getAllPrincipals().stream()
        .filter(principal -> principal instanceof DiscodeitUserDetails) // UserDetails 타입만 필터링
        .map(principal -> (DiscodeitUserDetails) principal) // UserDetails 타입으로 형변환 (캐스팅)
        .filter(userDetails -> userDetails.getUserDto().id().equals(userId)) //ID가 일치하는 유저만 통과
        .flatMap(userDetails -> sessionRegistry.getAllSessions(userDetails, false)
            .stream()) // 해당 유저의 세션들만 평평하게 꺼냄
        .forEach(SessionInformation::expireNow); // 전부 만료
  }
}
