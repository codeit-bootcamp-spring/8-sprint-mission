package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Transactional
  @Override
  public ReadStatusDto create(ReadStatusCreateRequest readStatusCreateRequest) {
    log.info("[ReadStatusService] 읽음 상태 생성 시작 - 사용자 Id: {}, 채널 Id: {}",
        readStatusCreateRequest.userId(), readStatusCreateRequest.channelId());

    if (readStatusRepository.existsByUserIdAndChannelId(readStatusCreateRequest.userId(),
        readStatusCreateRequest.channelId())) {
      log.warn("[ReadStatusService] 읾음 상태 생성 실패 - 이미 존재하는 읽음 상태");
      throw new ReadStatusAlreadyExistsException(readStatusCreateRequest.userId(),
          readStatusCreateRequest.channelId());
    }

    User user = userRepository.findById(readStatusCreateRequest.userId())
        .orElseThrow(() -> {
          log.warn("[ReadStatusService] 읽음 상태 생성 실패 - 존재하지 않는 사용자 Id: {}",
              readStatusCreateRequest.userId());
          return new UserNotFoundException(readStatusCreateRequest.userId());
        });

    Channel channel = channelRepository.findById(readStatusCreateRequest.channelId())
        .orElseThrow(() -> {
          log.warn("[ReadStatusService] 읽음 상태 생성 실패 - 존재하지 않는 채널 Id: {}",
              readStatusCreateRequest.channelId());
          return new ChannelNotFoundException(readStatusCreateRequest.channelId());
        });
    Instant lastReadAt = readStatusCreateRequest.lastReadAt();

    ReadStatus readStatus = new ReadStatus(user, channel, lastReadAt);
    readStatusRepository.save(readStatus);

    log.info("[ReadStatusService] 읽음 상태 생성 완료 - Id: {}", readStatus.getId());
    return readStatusMapper.toDto(readStatus);
  }

  @Override
  public ReadStatusDto find(UUID readStatusId) {
    log.debug("[ReadStatusService] 읽음 상태 조회 시작 - Id: {}", readStatusId);

    return readStatusRepository.findById(readStatusId)
        .map(readStatusMapper::toDto)
        .orElseThrow(() -> {
          log.warn("[ReadStatusService] 읽음 상태 조회 실패 - 존재하지 않는 Id: {}", readStatusId);
          return new ReadStatusNotFoundException(readStatusId);
        });
  }

  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    log.debug("[ReadStatusService] 특정 사용자의 읽음 상태 목록 조회 시작 - 사용자 Id: {}", userId);

    return readStatusRepository.findAllByUserIdWithFetchJoin(userId).stream()
        .map(readStatusMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest readStatusUpdateRequest) {
    log.info("[ReadStatusService] 읽음 상태 수정 시작 - Id: {}", readStatusId);

    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> {
          log.warn("[ReadStatusService] 읽음 상태 수정 실패 - 존재하지 않는 Id: {}", readStatusId);
          return new ReadStatusNotFoundException(readStatusId);
        });

    Instant lastReadAt;
    boolean newNotificationEnabled;

    if (readStatusUpdateRequest.newLastReadAt() == null) {
      // 요청에 lastReadAt이 null이라는 것은 알림 활성 여부만 수정 요청이라는 뜻
      // -> 기존 lastReadAt 유지
      // 알림 활성 여부 수정일 경우 현재 상태와 반대로 갱신
      lastReadAt = readStatus.getLastReadAt();
      newNotificationEnabled = !readStatus.isNotificationEnabled();
      log.info("[ReadStatusService] 알림 상태 토글 요청 - 변경 전: {}, 변경 후: {}",
          readStatus.isNotificationEnabled(), newNotificationEnabled);
    } else {
      // null이 아닐 경우 사용자가 채팅을 마지막으로 읽은 시간만 수정이라는 뜻
      // -> 새로운 lastReadAt 갱신
      // 사용자의 마지막 채팅 읽은 시간 수정일 경우 알림 활성 여부 유지
      lastReadAt = readStatusUpdateRequest.newLastReadAt();
      newNotificationEnabled = readStatus.isNotificationEnabled();
      log.info("[ReadStatusService] 읽음 시간 갱신 요청 - New LastReadAt: {}", lastReadAt);
    }

    readStatus.update(lastReadAt, newNotificationEnabled);

    log.info("[ReadStatusService] 읽음 상태 수정 완료 - Id: {}", readStatus.getId());
    return readStatusMapper.toDto(readStatus);
  }

  @Transactional
  @Override
  public void delete(UUID readStatusId) {
    log.info("[ReadStatusService] 읽음 상태 삭제 시작 - Id: {}", readStatusId);

    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> {
          log.warn("[ReadStatusService] 읽음 상태 삭제 실패: 존재하지 않는 Id: {}", readStatusId);
          return new ReadStatusNotFoundException(readStatusId);
        });

    readStatusRepository.delete(readStatus);
    log.info("[ReadStatusService] 읽음 상태 삭제 완료 - Id: {}", readStatus.getId());
  }
}
