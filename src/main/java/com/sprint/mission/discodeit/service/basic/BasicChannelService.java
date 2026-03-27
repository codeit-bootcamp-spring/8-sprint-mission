package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Transactional
  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public ChannelDto create(PublicChannelCreateRequest channelCreateRequest) {
    log.info("[ChannelService] 공개 채널 생성 시작 - 이름: {}", channelCreateRequest.name());

    String name = channelCreateRequest.name();
    String description = channelCreateRequest.description();

    Channel channel = new Channel(ChannelType.PUBLIC, name, description);
    channelRepository.save(channel);

    log.info("[ChannelService] 공개 채널 생성 완료 - Id: {}", channel.getId());
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  public ChannelDto create(PrivateChannelCreateRequest channelCreateRequest) {
    log.info("[ChannelService] 비공개 채널 생성 시작 - 참여자 수: {}",
        channelCreateRequest.participantIds().size());

    Channel channel = new Channel(ChannelType.PRIVATE);
    channelRepository.save(channel);

    List<ReadStatus> readStatusList = userRepository.findAllById(
            channelCreateRequest.participantIds())
        .stream()
        .map(user -> new ReadStatus(user, channel, channel.getCreatedAt()))
        .toList();

    readStatusRepository.saveAll(readStatusList);

    log.info("[ChannelService] 비공개 채널 생성 완료 - Id: {}, 생성된 읽음 상태 개수: {}", channel.getId(),
        readStatusList.size());
    return channelMapper.toDto(channel);
  }

  @Override
  public ChannelDto find(UUID channelId) {
    log.debug("[ChannelService] 채널 조회 시작 - Id: {}", channelId);

    return channelRepository.findById(channelId)
        .map(channelMapper::toDto)
        .orElseThrow(() -> {
          log.warn("[ChannelService] 채널 조회 실패 - 존재하지 않는 ID: {}", channelId);
          return new ChannelNotFoundException(channelId);
        });
  }

  @Transactional
  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest channelUpdateRequest) {
    log.info("[ChannelService] 채널 수정 시작 - Id: {}", channelId);

    String newName = channelUpdateRequest.newName();
    String newDescription = channelUpdateRequest.newDescription();

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("[ChannelService] 채널 수정 실패 - 존재하지 않는 ID: {}", channelId);
          return new ChannelNotFoundException(channelId);
        });

    if (channel.getType().equals(ChannelType.PRIVATE)) {
      log.warn("[ChannelService] 채널 수정 실패 - 비공개 채널인 경우 정보 수정 불가 - Id: {}", channelId);
      throw new PrivateChannelUpdateException();
    }

    channel.update(newName, newDescription);

    log.info("[ChannelService] 채널 수정 완료 - Id: {}", channelId);
    return channelMapper.toDto(channel);
  }

  @Transactional
  @Override
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public void delete(UUID channelId) {
    log.info("[ChannelService] 채널 삭제 시작 - Id: {}", channelId);

    if (!channelRepository.existsById(channelId)) {
      log.warn("[ChannelService] 채널 삭제 실패 - 존재하지 않는 Id: {}", channelId);
      throw new ChannelNotFoundException(channelId);
    }

    messageRepository.deleteAllByChannelId(channelId);
    readStatusRepository.deleteAllByChannelId(channelId);
    channelRepository.deleteById(channelId);

    log.info("[ChannelService] 채널 삭제 및 연관 정보(메시지, 읽음상태) 삭제 완료 - Id: {}", channelId);
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    log.debug("[ChannelService] 특정 사용자가 볼 수 있는 채널 목록 조회 시작 - UserId: {}", userId);

    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserIdWithFetchJoin(userId)
        .stream()
        .map(readStatus -> readStatus.getChannel().getId())
        .toList();

    List<ChannelDto> result = channelRepository.findAllPublicOrSubscribed(mySubscribedChannelIds)
        .stream()
        .map(channelMapper::toDto)
        .toList();

    log.info("[ChannelService] 특정 사용자가 볼 수 있는 채널 목록 조회 완료 - UserId: {}, 조회된 목록 개수: {}개",
        userId, result.size());
    return result;
  }
}
