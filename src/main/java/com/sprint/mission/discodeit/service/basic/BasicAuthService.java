package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails) {

        log.info("[AuthService] 현재 사용자 정보 조회 요청");

        if (userDetails == null) {
            log.info("[AuthService] UserDetails가 null입니다.");
            throw new AuthenticationCredentialsNotFoundException("인증 정보가 존재하지 않습니다.");
        }

        // UserDetails에서 username 추출
        String username = userDetails.getUsername();
        log.info("[AuthService] 조회할 사용자 명: {}", username);

        UUID userId = userDetails.getUserDto().id();

        // Repository를 통해 최신 사용자 정보 조회 (권한 변경 등이 실시간으로 반영됨)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("[AuthService] 사용자를 찾을 수 없습니다: " + username));

        log.info("[AuthService] 조회된 사용자 정보: {}", user);

        // Entity를 DTO로 변환 (Service에서 처리)
        UserDto userDto = userMapper.toDto(user, true);

        log.info("[AuthService] 변환 완료");

        return userDto;
    }
}
