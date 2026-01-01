package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.LoginRequest;
import com.sprint.mission.discodeit.DTO.response.UserResponse;
import com.sprint.mission.discodeit.Exception.AlreadyLoggedInException;
import com.sprint.mission.discodeit.Exception.AlreadyLoggedOutException;
import com.sprint.mission.discodeit.Exception.InvalidPasswordException;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new NoSuchElementException("해당 유저가 존재하지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NoSuchElementException("해당하는 userStatus가 없습니다."));

        if(status.isCurrentOnline()) {
            throw new AlreadyLoggedInException();
        }

        status.markOnline();
        userStatusRepository.save(status);

        return UserResponse.from(user, status);
    }

    @Override
    public String logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 유저가 존재하지 않습니다."));

        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NoSuchElementException("해당하는 userStatus가 없습니다."));

        if(!status.isCurrentOnline()) {
            throw new AlreadyLoggedOutException();
        }

        status.markOffline();
        userStatusRepository.save(status);

        return user.getName();
    }
}
