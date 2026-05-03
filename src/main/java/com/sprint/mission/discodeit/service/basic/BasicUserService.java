package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.cache.CacheNames;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

		private final UserRepository userRepository;
		private final UserMapper userMapper;
		private final PasswordEncoder passwordEncoder;
		private final BinaryContentRepository binaryContentRepository;
		private final BinaryContentStorage binaryContentStorage;
		private final JwtRegistry jwtRegistry;
		private final ApplicationEventPublisher applicationEventPublisher;

		/**
		 * 사용자 생성 (이메일/사용자명 중복 시 예외).
		 */
		@Transactional
		@Override
		@CacheEvict(cacheNames = CacheNames.USERS_ALL, allEntries = true)
		public UserDto create(UserCreateRequest userCreateRequest,
				Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
				String username = userCreateRequest.username();
				String email = userCreateRequest.email();
				log.debug("사용자 생성 시도, username={}, email={}", username, email);

				if (userRepository.existsByEmail(email)) {
						log.warn("사용자 생성 실패: 이메일 중복, email={}", email);
						throw new IllegalArgumentException("User with email " + email + " already exists");
				}
				if (userRepository.existsByUsername(username)) {
						log.warn("사용자 생성 실패: 사용자명 중복, username={}", username);
						throw new IllegalArgumentException(
								"User with username " + username + " already exists");
				}

				BinaryContent nullableProfile = optionalProfileCreateRequest
						.map(profileRequest -> {
								String fileName = profileRequest.fileName();
								String contentType = profileRequest.contentType();
								byte[] bytes = profileRequest.bytes();
								BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
										contentType);
								binaryContentRepository.save(binaryContent);
								persistProfileBinaryToStorage(binaryContent, bytes);
								return binaryContent;
						})
						.orElse(null);
				String password = passwordEncoder.encode(userCreateRequest.password());

				User user = new User(username, email, password, Role.USER, nullableProfile);
				userRepository.save(user);
				log.info("사용자 생성 완료, id={}, username={}", user.getId(), username);
				return withOnline(userMapper.toDto(user));
		}

		@Override
		public UserDto find(UUID userId) {
				return userRepository.findById(userId)
						.map(userMapper::toDto)
						.map(this::withOnline)
						.orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
		}

		@Override
		@Transactional(readOnly = true)
		@Cacheable(cacheNames = CacheNames.USERS_ALL, key = "'all'")
		public List<UserDto> findAll() {
				return userRepository.findAllWithProfile()
						.stream()
						.map(userMapper::toDto)
						.map(this::withOnline)
						.toList();
		}

		/**
		 * 사용자 수정 (대상 없거나 이메일/사용자명 중복 시 예외).
		 */
		@Transactional
		@Override
		@CacheEvict(cacheNames = CacheNames.USERS_ALL, allEntries = true)
		@PreAuthorize("@securityExpressionService.isCurrentUser(#userId, authentication)")
		public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
				Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
				log.debug("사용자 수정 시도, userId={}", userId);
				User user = userRepository.findById(userId)
						.orElseThrow(() -> {
								log.warn("사용자 수정 실패: 사용자 없음, userId={}", userId);
								return new NoSuchElementException("User with id " + userId + " not found");
						});

				String newUsername = userUpdateRequest.newUsername();
				String newEmail = userUpdateRequest.newEmail();
				if (newEmail != null && !newEmail.equals(user.getEmail()) && userRepository.existsByEmail(
						newEmail)) {
						log.warn("사용자 수정 실패: 이메일 중복, newEmail={}", newEmail);
						throw new IllegalArgumentException("User with email " + newEmail + " already exists");
				}
				if (newUsername != null && !newUsername.equals(user.getUsername())
						&& userRepository.existsByUsername(newUsername)) {
						log.warn("사용자 수정 실패: 사용자명 중복, newUsername={}", newUsername);
						throw new IllegalArgumentException(
								"User with username " + newUsername + " already exists");
				}

				BinaryContent nullableProfile = optionalProfileCreateRequest
						.map(profileRequest -> {
								String fileName = profileRequest.fileName();
								String contentType = profileRequest.contentType();
								byte[] bytes = profileRequest.bytes();
								BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
										contentType);
								binaryContentRepository.save(binaryContent);
								persistProfileBinaryToStorage(binaryContent, bytes);
								return binaryContent;
						})
						.orElse(null);

				String newPassword = userUpdateRequest.newPassword();
				if (newPassword != null) {
						newPassword = passwordEncoder.encode(newPassword);
				}
				user.update(newUsername, newEmail, newPassword, nullableProfile);
				log.info("사용자 수정 완료, userId={}, username={}", userId, newUsername);
				return withOnline(userMapper.toDto(user));
		}

		@Transactional
		@Override
		@CacheEvict(cacheNames = CacheNames.USERS_ALL, allEntries = true)
		@PreAuthorize("hasRole('ADMIN')")
		public UserDto updateRole(UUID userId, Role newRole) {
				User user = userRepository.findById(userId)
						.orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
				Role previousRole = user.getRole();
				user.updateRole(newRole);
				// 권한 변경 시 토큰 강제 만료 처리로 로그아웃 유도
				jwtRegistry.invalidateJwtInformationByUserId(userId);
				if (previousRole != newRole) {
						applicationEventPublisher.publishEvent(
								new RoleUpdatedEvent(userId, previousRole, newRole));
				}
				log.info("사용자 권한 변경 완료, userId={}, role={}", userId, newRole);
				return withOnline(userMapper.toDto(user));
		}

		/**
		 * 사용자 삭제 (대상 없으면 예외).
		 */
		@Transactional
		@Override
		@CacheEvict(cacheNames = CacheNames.USERS_ALL, allEntries = true)
		@PreAuthorize("@securityExpressionService.isCurrentUser(#userId, authentication)")
		public void delete(UUID userId) {
				log.debug("사용자 삭제 시도, userId={}", userId);
				if (!userRepository.existsById(userId)) {
						log.warn("사용자 삭제 실패: 사용자 없음, userId={}", userId);
						throw new NoSuchElementException("User with id " + userId + " not found");
				}
				userRepository.deleteById(userId);
				log.info("사용자 삭제 완료, userId={}", userId);
		}

		private UserDto withOnline(UserDto dto) {
				return new UserDto(
						dto.id(),
						dto.username(),
						dto.email(),
						dto.profile(),
						jwtRegistry.hasActiveJwtInformationByUserId(dto.id()),
						dto.role()
				);
		}

		/**
		 * 프로필은 UI가 곧바로 조회하므로 이벤트 비동기 저장 대신 동기로 스토리지에 기록한다.
		 */
		private void persistProfileBinaryToStorage(BinaryContent binaryContent, byte[] bytes) {
				binaryContentStorage.put(binaryContent.getId(), bytes);
				binaryContent.updateStatus(BinaryContentStatus.SUCCESS);
				binaryContentRepository.save(binaryContent);
		}

}
