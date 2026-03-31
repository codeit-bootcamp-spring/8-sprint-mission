package com.sprint.mission.discodeit.service.auth;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

    private final UserDto userDto;
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + userDto.role().name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userDto.username();
    }

    @Override
    public boolean equals(Object o) {// 자기 자신과 비교
        if (this == o) return true;// 타입 비교
        if (!(o instanceof DiscodeitUserDetails that)) return false;// 사용자 이름 비교
        return Objects.equals(userDto.username(), that.userDto.username());
    }

    @Override
    public int hashCode() {
        // 사용자 이름 해시 코드 반환
        return Objects.hashCode(userDto.username());
    }
}
