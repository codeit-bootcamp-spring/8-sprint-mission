package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.ReadStatusDto;
import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    UUID userId = request.userId();
    UUID channelId = request.channelId();

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException(userId + " 해당하는 ReadStatus가 존재하지 않습니다."));
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException(channelId + " 해당하는 ReadStatus가 존재하지 않습니다."));

    if (readStatusRepository.existsByUserIdAndChannelId(userId, channelId)) {
      throw new IllegalStateException(userId + " 와 " + channelId + " 해당하는 ReadStatus가 존재합니다.");
    }

    Instant now = Instant.now();

    ReadStatus savedReadStatus = new ReadStatus(
        user,
        channel,
        now
    );

    return readStatusMapper.toDto(readStatusRepository.save(savedReadStatus));
  }

  @Override
  public ReadStatusDto find(UUID readStatusId) {
    return readStatusRepository.findById(readStatusId)
        .map(readStatusMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException(readStatusId + " 해당하는 readSatus 존재하지 않습니다."));
  }

  //N+1 문제 발생할 수 있음
  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUserIdWithChannel(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    Instant newLastReadAt = request.newLastReadAt();
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(
            () -> new NoSuchElementException(readStatusId + " 해당하는 readStatus가 존재하지 않습니다."));

    readStatus.update(newLastReadAt);

    return readStatusMapper.toDto(readStatusRepository.save(readStatus));
  }

  @Override
  @Transactional
  public void delete(UUID readStatusId) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(
            () -> new NoSuchElementException(readStatusId + " 해당하는 readStatus가 존재하지 않습니다."));

    readStatusRepository.delete(readStatus);
  }
}
