package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.UserDto;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

  private final UserDto userDto;
  private final String password;

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getUsername() {
    return userDto.username();
  }

  @Override
  public int hashCode() {
    return Objects.hash(userDto.username());
  }

  @Override
  public boolean equals(Object obj) {
    // 자기 자신과 비교
    if (this == obj) {
      return true;
    }
    // 타입 비교
    if (!(obj instanceof DiscodeitUserDetails that)) {
      return false;
    }
    // 사용자 이름 비교
    return Objects.equals(userDto.username(), that.userDto.username());
  }
}
