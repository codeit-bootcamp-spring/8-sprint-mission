package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  // 설계 원칙: 다른 Service 대신 필요한 Repository 의존성을 직접 주입
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  public ReadStatus create(ReadStatusCreateRequest request) {
    // 1. 관련 Channel이나 User 존재 여부 검증
    userRepository.findById(request.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

    // 2. 이미 동일한 User-Channel 쌍이 존재하는지 검증
    boolean exists = readStatusRepository.findAllByUserId(request.getUserId()).stream()
        .anyMatch(rs -> rs.getChannelId().equals(request.getChannelId()));

    if (exists) {
      throw new IllegalStateException("해당 유저의 채널 읽음 상태가 이미 존재합니다.");
    }

    // User와 Channel 조회
    com.sprint.mission.discodeit.entity.User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

    ReadStatus readStatus;
    // API 명세서에 맞춘 lastReadAt 필드 우선 사용
    if (request.getLastReadAt() != null) {
      readStatus = new ReadStatus(user, channel, request.getLastReadAt());
    } else {
      // 하위 호환성을 위한 lastReadMessageId 사용 (현재 시간으로 설정)
      readStatus = new ReadStatus(user, channel, java.time.Instant.now());
    }

    return readStatusRepository.save(readStatus);
  }

  @Override
  public Optional<ReadStatus> findById(UUID id) {
    return readStatusRepository.findById(id);
  }

  @Override
  public List<ReadStatus> findAllByUserId(UUID userId) {
    return readStatusRepository.findAllByUserId(userId);
  }

  @Override
  public ReadStatus update(ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(request.getId())
        .orElseThrow(() -> new IllegalArgumentException("읽음 상태 객체를 찾을 수 없습니다."));

    // API 명세서에 맞춘 lastReadAt 업데이트
    if (request.getNewLastReadAt() != null) {
      readStatus.updateLastReadAt(request.getNewLastReadAt());
    }

    // 하위 호환성을 위한 lastReadMessageId 업데이트
    if (request.getLastReadMessageId() != null) {
      readStatus.updateLastReadMessage(request.getLastReadMessageId());
    }

    return readStatusRepository.save(readStatus);
  }

  @Override
  public void delete(UUID id) {
    readStatusRepository.delete(id);
  }
}