package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.response.ReadStatusResponse;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
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

@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        User user = userRepository.findById(request.uId())
                .orElseThrow(()-> new NoSuchElementException("해당하는 유저가 없습니다."));
        Channel channel = channelRepository.findById(request.cId())
                .orElseThrow(()-> new NoSuchElementException("해당하는 채널이 없습니다."));

        boolean alreadyExists = readStatusRepository.existsByUserIdAndChannelId(
                request.uId(),
                request.cId()
        );

        if (alreadyExists) {
            throw new IllegalStateException("이미 해당 채널에 대한 ReadStatus가 존재합니다.");
        }
        Instant lastMessageReadAt = request.newLastMessageReadAt();
        ReadStatus readStatus = new ReadStatus(
                request.uId(),
                request.cId(),
                lastMessageReadAt
        );

        ReadStatus savedRead = readStatusRepository.save(readStatus);
        return  ReadStatusResponse.from(savedRead);
    }

    public ReadStatus find(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readSatus입니다."));
    }

    public List<ReadStatus> findAllByUserId(UUID uId) {
        return readStatusRepository.findAllByUserId(uId);
    }

    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readStatus"));
        readStatus.update(request.newLastMessageReadAt());
        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);
        return ReadStatusResponse.from(savedReadStatus);
    }

    public void delete(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 readStatus입니다."));
        readStatusRepository.delete(id);
    }
}
