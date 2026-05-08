package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.Sse.BinaryContent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserUpdatedEvent;
import com.sprint.mission.discodeit.exception.UserException.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.UserException.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtRegistry jwtRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    @Override
    public UserDto create(UserCreateRequest request,
                          Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String username = request.username();
        String email = request.email();

        log.info("Service: 유저 생성 로직 시작 - email: {}, username: {}", username, email);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        BinaryContent profile = optionalProfileCreateRequest
                .map(this::saveBinaryContent)
                .orElse(null);

        if (profile != null) {
            createdEvent(profile.getId(), optionalProfileCreateRequest.get().bytes());
            log.debug("Service: 사용자 프로필 이미지 이벤트 발행 완료 - ID: {}", profile.getId());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(username, email, encodedPassword, profile);
        User savedUser = userRepository.save(user);

        UserDto userDto = userMapper.toDto(savedUser);

        eventPublisher.publishEvent(new UserCreatedEvent(userDto, savedUser.getCreatedAt()));

        log.info("Service: 유저 생성 완료 및 DB 저장 완료 - ID: {}", savedUser.getId());

        return userDto;
    }

    @Override
    public UserDto find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toDto(user);
    }

    @Cacheable(value = "users", key = "'all'", unless = "#result.isEmpty()")
    @Override
    public List<UserDto> findAll() {
        return userRepository.findAllWithProfile().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @CacheEvict(value = "users", allEntries = true)
    @PreAuthorize("principal.userDto.id == #userId")
    @Transactional
    @Override
    public UserDto update(UUID userId, UserUpdateRequest request,
                          Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String newUsername = request.newUsername();
        String newEmail = request.newEmail();

        boolean requiredRelogin = false;

        log.info("Service: 유저 수정 요청 - ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Service: 유저 수정 실패(존재하지 않는 ID) - ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        // 기존 유저의 이름과 요청한 이름이 다를 경우, 같으면 넘어감
        if (!user.getUsername().equals(newUsername)) {
            if (userRepository.existsByUsername(newUsername)) {
                log.warn("Service: 유저 수정 실패(이미 해당 유저 이름 존재) - username: {}", newUsername);
                throw new DuplicateUsernameException(newUsername);
            }
            requiredRelogin = true;
        }

        // 기존 유저의 이메일과 요청한 이메일 다를 경우, 같으면 넘어감
        if (!user.getEmail().equals(newEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                log.warn("Service: 유저 수정 실패(이미 해당 유저 이메일 존재) - email: {}", newEmail);
                throw new DuplicateEmailException(newEmail);
            }
        }

        BinaryContent newProfile = user.getProfile();
        if (optionalProfileCreateRequest.isPresent()) {
            if (user.getProfile() != null) {
                //byte를 저장해놓은 기존 파일 삭제
                binaryContentRepository.delete(user.getProfile());
            }
            newProfile = saveBinaryContent(optionalProfileCreateRequest.get());
            createdEvent(newProfile.getId(), optionalProfileCreateRequest.get().bytes());
            log.debug("Service: 사용자 프로필 수정 이벤트 발행 완료 - ID: {}", newProfile.getId());
        }

        String encodedPassword = user.getPassword();
        if (request.newPassword() != null) {
            encodedPassword = passwordEncoder.encode(request.newPassword());
            requiredRelogin = true;
            log.debug("Service: 사용자 비밀번호 수정 및 암호화 완료");
        }

        UserDto prevUserDto = userMapper.toDto(user);

        user.update(request.newUsername(), request.newEmail(), encodedPassword, newProfile);

        UserDto userDto = userMapper.toDto(user);

        eventPublisher.publishEvent(new UserUpdatedEvent(prevUserDto, userDto, user.getUpdatedAt()));

        if (requiredRelogin) {
            jwtRegistry.invalidateJwtInformationByUserId(userId);
            log.info("Service: 주요 정보 (username/password) 변경으로 기존 토큰 무효화 ID: {}", userId);
        }

        log.info("Service: 유저 수정 완료 - ID: {}", userId);

        return userDto;
    }

    @CacheEvict(value = "users", allEntries = true)
    @PreAuthorize("principal.userDto.id == #userId")
    @Transactional
    @Override
    public void delete(UUID userId) {
        UserDto userDto = find(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Service - 사용자 삭제 실패(존재하지 않는 유저) - ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        if (user.getProfile() != null) {
            binaryContentRepository.deleteById(user.getProfile().getId());
            log.debug("Service - 사용자 프로필 이미지 데이터 삭제 완료");
        }

        jwtRegistry.invalidateJwtInformationByUserId(userId);
        log.info("Service: 회원 탈퇴로 인한 토큰 삭제 완료 - ID: {}", userId);

        readStatusRepository.deleteAllByUserId(userId);
        messageRepository.deleteAllByAuthorId(userId);
        userRepository.delete(user);

        eventPublisher.publishEvent(new UserDeletedEvent(userDto, Instant.now()));

        log.info("Service: 사용자 DB 삭제 완료 - ID: {}", userId);
    }

    //중복 코드 제거
    private BinaryContent saveBinaryContent(BinaryContentCreateRequest request) {
        String fileName = request.fileName();
        String contentType = request.contentType();
        byte[] bytes = request.bytes();
        BinaryContent binaryContent = new BinaryContent(
                fileName,
                (long) bytes.length,
                contentType,
                BinaryContentStatus.PROCESSING
        );
        return binaryContentRepository.save(binaryContent);
    }

    // 생성 이벤트 발행 중복 코드
    private void createdEvent(UUID id, byte[] bytes) {
        BinaryContentCreatedEvent event = new BinaryContentCreatedEvent(
                id,
                bytes
        );
        eventPublisher.publishEvent(event);
    }
}
