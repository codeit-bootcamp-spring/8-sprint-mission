package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
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

    private final UserStatusRepository userStatusRepository;

    @Override
    public UserStatusResponse update(UserStatusRequest request) {
        UserStatus userStatus = userStatusRepository.findByUserId(request.getUserId())
                .orElse(new UserStatus(request.getUserId()));

        userStatus.updateLastAccessAt();
        UserStatus saved = userStatusRepository.save(userStatus);

        return convertToResponse(saved);
    }

    @Override
    public UserStatus create(UUID userId) {
        return null;
    }

    @Override
    public UserStatus create(UserStatusCreateRequest request) {
        return null;
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<UserStatus> findAll() {
        return List.of();
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return Optional.empty();
    }

    @Override
    public UserStatus update(UserStatusUpdateRequest request) {
        return null;
    }

    @Override
    public UserStatus updateByUserId(UUID userId, boolean isOnline) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }

    private UserStatusResponse convertToResponse(UserStatus userStatus) {
        return UserStatusResponse.builder()
                .userId(userStatus.getUserId())
                .isOnline(userStatus.isOnline())
                .lastAccessAt(userStatus.getLastAccessAt())
                .build();
    }
}