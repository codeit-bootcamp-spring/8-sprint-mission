package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.DTO.response.UserResponse;
import com.sprint.mission.discodeit.DTO.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(UserCreateRequest request, Optional<BinaryContentCreateRequest> requestDTO) {
        if (userRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 username입니다.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 email입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        UUID profileIdNullable = requestDTO
                .map(profileRequest -> {
                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent binaryContent = new BinaryContent(fileName, (long)bytes.length, contentType, bytes);
                    return binaryContentRepository.save(binaryContent).getId();
                })
                .orElse(null);

        User user = new User(
                request.userId(),
                request.name(),
                encodedPassword,
                request.email(),
                request.gender(),
                request.grade(),
                profileIdNullable
        );

        User savedUser = userRepository.save(user);
        UserStatus status = new UserStatus(savedUser.getId());
        userStatusRepository.save(status);

        return UserResponse.from(savedUser, status);
    }


    @Override
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 유저를 찾을 수 없습니다."));

        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NoSuchElementException("해당하는 userStatus를 찾을 수 없습니다."));

        return UserResponse.from(user, status);
    }

    @Override
    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    UserStatus status = userStatusRepository.findByUserId(user.getId())
                            .orElseThrow(()-> new NoSuchElementException("해당하는 userStatus를 찾을 수 없습니다."));
                    return UserResponse.from(user, status);
                })
                .toList();
    }

    @Override
    public Map<Integer, List<User>> findUserByGrade() {
        return Map.of();
    }

    @Override
    public UserResponse update(UserUpdateRequest request, Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new NoSuchElementException("수정하려는 유저가 없습니다."));

        if (userRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미" + request.name() + "을 가진 유저가 존재합니다.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미" + request.email()+ "을 가진 유저가 존재합니다.");
        }

        UUID newProfileId = optionalProfileCreateRequest
                .map(profileRequest -> {
                    Optional.ofNullable(user.getProfileId())
                            .ifPresent(binaryContentRepository::delete);

                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent binaryContent = new BinaryContent(fileName, (long)bytes.length, contentType, bytes);
                    return binaryContentRepository.save(binaryContent).getId();
                })
                .orElse(null);



        user.update(request.userId(), request.name(), request.email(), request.password(), request.gender(), request.grade(), newProfileId);

        User savedUser = userRepository.save(user);
        UserStatus status = userStatusRepository.findByUserId(savedUser.getId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 UserStatus입니다."));
        return UserResponse.from(savedUser, status);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("삭제하려는 유저가 없습니다."));

        userStatusRepository.deleteByUserId(user.getId());

        if(user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }

        userRepository.delete(id);
    }
}
