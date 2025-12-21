package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    // 설계 원칙: 다른 Service 대신 필요한 Repository 의존성을 직접 주입
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatus create(UserStatusCreateRequest request) {
        // 1. 관련된 User가 존재하지 않으면 예외 발생
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2. 같은 User와 관련된 객체가 이미 존재하면 예외 발생
        if (userStatusRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalStateException("해당 유저의 상태 객체가 이미 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(request.getUserId(), request.isOnline());
        return userStatusRepository.save(userStatus);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return userStatusRepository.findById(id);
    }

    @Override
    public List<UserStatus> findAll() {
        return userStatusRepository.findAll();
    }

    @Override
    public UserStatus update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("상태 객체를 찾을 수 없습니다."));

        userStatus.updateStatus(request.isOnline());
        return userStatusRepository.save(userStatus);
    }

    @Override
    public UserStatus updateByUserId(UUID userId, boolean isOnline) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 상태 정보를 찾을 수 없습니다."));

        userStatus.updateStatus(isOnline);
        return userStatusRepository.save(userStatus);
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.delete(id);
    }
}