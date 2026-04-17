package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.BinaryContentDeletedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
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

        log.info("Service: 유저 생성 완료 및 DB 저장 완료 - ID: {}", savedUser.getId());


        return userMapper.toDto(savedUser, false);
    }

    @Override
    public UserDto find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean isOnline = isUserOnline(user.getId());
        return userMapper.toDto(user, isOnline);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    boolean isOnline = isUserOnline(user.getId());
                    return userMapper.toDto(user, isOnline);
                })
                .toList();
    }

    @PreAuthorize("principal.userDto.id == #userId")
    @Override
    @Transactional
    public UserDto update(UUID userId, UserUpdateRequest request,
                          Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String newUsername = request.newUsername();
        String newEmail = request.newEmail();

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
                UUID userProfileId = user.getProfile().getId();
                //byte를 저장해놓은 기존 파일 삭제
                deletedEvent(newProfile.getId());
                binaryContentRepository.delete(user.getProfile());
            }
            newProfile = saveBinaryContent(optionalProfileCreateRequest.get());
            createdEvent(newProfile.getId(), optionalProfileCreateRequest.get().bytes());
            log.debug("Service: 사용자 프로필 수정 이벤트 발행 완료 - ID: {}", newProfile.getId());
        }

        String encodedPassword = user.getPassword();
        if (request.newPassword() != null) {
            encodedPassword = passwordEncoder.encode(request.newPassword());
            log.debug("Service: 사용자 비밀번호 수정 및 암호화 완료");
        }

        user.update(request.newUsername(), request.newEmail(), encodedPassword, newProfile);

        log.info("Service: 유저 수정 완료 - ID: {}", userId);

        boolean isOnline = isUserOnline(user.getId());
        return userMapper.toDto(userRepository.save(user), isOnline);
    }

    @PreAuthorize("principal.userDto.id == #userId")
    @Override
    @Transactional
    public void delete(UUID userId) {
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
        log.info("Service: 사용자 DB 삭제 완료 - ID: {}", userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public UserDto updateUserRole(UUID userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Role oldRole = user.getRole();

        user.updateRole(newRole);

        updatedEvent(userId, oldRole, newRole);

        jwtRegistry.invalidateJwtInformationByUserId(userId);

        log.info("[UserService] 사용자 권한 변경 및 JWT 무효화 완료!");

        // 세션이 무효화되어 오프라인 처리
        return userMapper.toDto(user, false);
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

    // 삭제 이벤트 발생
    private void deletedEvent(UUID id) {
        BinaryContentDeletedEvent event = new BinaryContentDeletedEvent(
                id
        );
        eventPublisher.publishEvent(event);
    }

    // 사용자의 권한 변경 후 이벤트 발생
    private void updatedEvent(UUID id, Role oldRole, Role newRole) {
        RoleUpdatedEvent event = new RoleUpdatedEvent(
                id,
                oldRole,
                newRole
        );
        eventPublisher.publishEvent(event);
    }

    private boolean isUserOnline(UUID userId) {
        return jwtRegistry.hasActiveJwtInformationByUserId(userId);
    }
}
