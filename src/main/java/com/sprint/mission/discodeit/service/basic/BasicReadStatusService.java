package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.response.ReadStatusResponse;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.Exception.NotExistReadStatusException;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
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

@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()-> new NoSuchElementException("해당하는 유저가 없습니다."));
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(()-> new NoSuchElementException("해당하는 채널이 없습니다."));

        boolean alreadyExists = readStatusRepository.existsByUserIdAndChannelId(
                request.userId(),
                request.channelId()
        );

        if (alreadyExists) {
            throw new IllegalStateException("이미 해당 채널에 대한 ReadStatus가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(
                request.userId(),
                request.channelId(),
                Instant.now()
        );

        ReadStatus savedRead = readStatusRepository.save(readStatus);
        return  ReadStatusResponse.from(savedRead);
    }

    @Override
    public ReadStatus find(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readSatus입니다."));
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId).stream()
                .map(ReadStatusResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readStatus"));

        Instant newLastReadAtMessageAt = request.newLastMessageReadAt() == null ? Instant.now() : request.newLastMessageReadAt();
        readStatus.update(request.newLastMessageReadAt());

        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);
        return ReadStatusResponse.from(savedReadStatus);
    }

    @Override
    public ReadStatusResponse update(UUID userId, UUID channelId, ReadStatusUpdateRequest readStatusUpdateRequest) {
        ReadStatus readStatus = readStatusRepository.findByUserIdAndChannelId(userId, channelId)
                .orElseThrow(NotExistReadStatusException::new);
        Instant newLastMessageReadAt = readStatusUpdateRequest.newLastMessageReadAt() == null ?  Instant.now() : readStatusUpdateRequest.newLastMessageReadAt();

        readStatus.update(newLastMessageReadAt);
        readStatusRepository.save(readStatus);

        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public void delete(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readStatus입니다."));
        readStatusRepository.delete(id);
    }
}
