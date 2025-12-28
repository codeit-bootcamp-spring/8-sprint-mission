package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
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
                .orElseThrow(() -> new IllegalArgumentException("유저 상태를 찾을 수 없습니다."));

        userStatus.updateLastAccessAt();
        UserStatus saved = userStatusRepository.save(userStatus);

        return convertToResponse(saved);
    }

    @Override
    public UserStatus create(UUID userId) {
        return userStatusRepository.save(new UserStatus(userId));
    }

    @Override
    public Optional<UserStatus> findById(UUID id) { return userStatusRepository.findById(id); }

    @Override
    public List<UserStatus> findAll() { return userStatusRepository.findAll(); }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) { return userStatusRepository.findByUserId(userId); }

    @Override
    public void delete(UUID id) { userStatusRepository.delete(id); }

    private UserStatusResponse convertToResponse(UserStatus userStatus) {
        return UserStatusResponse.builder()
                .userId(userStatus.getUserId())
                .isOnline(userStatus.isOnline())
                .lastAccessAt(userStatus.getLastAccessAt())
                .build();
    }
}