package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
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

@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  public ReadStatus create(ReadStatusCreateRequest request) {
    UUID userId = request.userId();
    UUID channelId = request.channelId();

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException(userId + "에 해당하는 ReadStatus가 존재하지 않습니다."));
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException(channelId + "에 해당하는 ReadStatus가 존재하지 않습니다."));

    if (readStatusRepository.existsByUserIdAndChannelId(userId, channelId)) {
      throw new IllegalStateException(userId + " 와 " + channelId + "의 ReadStatus가 존재합니다.");
    }

    ReadStatus readStatus = new ReadStatus(
        request.userId(),
        request.channelId(),
        request.lastReadAt()
    );

    return readStatusRepository.save(readStatus);
  }

  @Override
  public ReadStatus find(UUID id) {
    return readStatusRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException(id + "가 존재하지 않는 readSatus입니다."));
  }

  @Override
  public List<ReadStatus> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUserId(userId).stream()
        .toList();
  }

  @Override
  public ReadStatus update(UUID id, ReadStatusUpdateRequest request) {
    Instant newLastReadAt = request.newLastReadAt();
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException(id + "에 해당하는 readStatus가 존재하지 않습니다."));

    readStatus.update(newLastReadAt);

    return readStatusRepository.save(readStatus);
  }

  @Override
  public void delete(UUID id) {
    ReadStatus readStatus = readStatusRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException(id + "에 해당하는 readStatus가 존재하지 않습니다."));
    readStatusRepository.deleteById(id);
  }
}
