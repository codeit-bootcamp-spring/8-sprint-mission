package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusDto create(ReadStatusCreateRequest request) {
        // 1) User 존재 여부 확인
        if (!userRepository.existsById(request.userId())) {
            throw new NoSuchElementException("User 를 찾을 수 없습니다: " + request.userId());
        }

        // 2) Channel 존재 여부 확인
        if (!channelRepository.existsById(request.channelId())) {
            throw new NoSuchElementException("Channel 을 찾을 수 없습니다: " + request.channelId());
        }

        // 3) (userId, channelId) 조합 중복 체크
        readStatusRepository.findByUserIdAndChannelId(request.userId(), request.channelId())
                .ifPresent(rs -> {
                    throw new IllegalStateException("이미 존재하는 ReadStatus 입니다.");
                });

        Instant lastReadAt = (request.lastReadAt() != null)
                ? request.lastReadAt()
                : Instant.EPOCH; // 기본값: 아주 옛날 시각

        ReadStatus readStatus = new ReadStatus(
                request.userId(),
                request.channelId(),
                lastReadAt
        );

        readStatusRepository.save(readStatus);

        return convertDto(readStatus);
    }

    @Override
    public ReadStatusDto findById(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ReadStatus를 찾을 수 없습니다. " + id));

        return convertDto(readStatus);
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId).stream()
                .map(this::convertDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReadStatusDto update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(request.id())
                .orElseThrow(() -> new NoSuchElementException("ReadStatus를 찾을 수 없습니다: " + request.id()));

        readStatus.update(request.lastReadAt());
        readStatusRepository.save(readStatus);

        return convertDto(readStatus);
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.deleteById(id);
    }

    private ReadStatusDto convertDto(ReadStatus entity) {
        return new ReadStatusDto(
                entity.getId(),
                entity.getUserId(),
                entity.getChannelId(),
                entity.getLastReadAt()
        );
    }
}
