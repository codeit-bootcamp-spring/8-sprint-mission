package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Override
  @Transactional
  public ReadStatusDto create(ReadStatusCreateRequest request) {

    // 1) User 엔티티 조회
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    // 2) Channel 엔티티 조회
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(
            () -> new ChannelNotFoundException(request.channelId()));

    // 3) (userId, channelId) 조합 중복 체크
    readStatusRepository.findByUser_IdAndChannel_Id(request.userId(), request.channelId())
        .ifPresent(rs -> {
          throw new ReadStatusNotFoundException(rs.getId());
        });

    Instant lastReadAt =
        (request.lastReadAt() != null) ? request.lastReadAt() : Instant.EPOCH; // 기본값: 아주 옛날 시각

    ReadStatus readStatus = new ReadStatus(user, channel, lastReadAt);

    ReadStatus savedStatus = readStatusRepository.save(readStatus);

    return readStatusMapper.toDto(savedStatus);
  }

  @Override
  public ReadStatusDto findById(UUID id) {
    return readStatusRepository.findById(id)
        .map(readStatusMapper::toDto)
        .orElseThrow(() -> new ReadStatusNotFoundException(id));
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUser_Id(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

    readStatus.update(request.newLastReadAt());

    return readStatusMapper.toDto(readStatus);
  }

  @Override
  @Transactional
  public void delete(UUID id) {

    if (!readStatusRepository.existsById(id)) {
      throw new ReadStatusNotFoundException(id);
    }
    readStatusRepository.deleteById(id);
  }
}
