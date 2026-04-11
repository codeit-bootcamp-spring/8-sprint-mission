package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscodeitUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
        .map(user -> new DiscodeitUserDetails(
            userMapper.toDto(user, true),
            user.getPassword()
        ))
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
  }

  // 추가: JWT 필터에서 UUID로 사용자를 찾을 때 사용
  @Transactional(readOnly = true)
  public UserDetails loadUserById(UUID id) {
    return userRepository.findById(id)
        .map(user -> new DiscodeitUserDetails(
            userMapper.toDto(user, true),
            user.getPassword()
        ))
        .orElseThrow(() -> new UsernameNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + id));
  }
}
