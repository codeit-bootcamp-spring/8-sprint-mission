package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  /** 공개 채널 생성. */
  @Transactional
  @Override
  public ChannelDto create(PublicChannelCreateRequest request) {
    String name = request.name();
    String description = request.description();
    log.debug("공개 채널 생성, name={}", name);
    Channel channel = new Channel(ChannelType.PUBLIC, name, description);
    channelRepository.save(channel);
    log.info("공개 채널 생성 완료, channelId={}, name={}", channel.getId(), name);
    return channelMapper.toDto(channel);
  }

  /** 비공개 채널 생성. */
  @Transactional
  @Override
  public ChannelDto create(PrivateChannelCreateRequest request) {
    log.debug("비공개 채널 생성, participantIds={}", request.participantIds());
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    channelRepository.save(channel);

    List<ReadStatus> readStatuses = userRepository.findAllById(request.participantIds()).stream()
        .map(user -> new ReadStatus(user, channel, channel.getCreatedAt()))
        .toList();
    readStatusRepository.saveAll(readStatuses);
    log.info("비공개 채널 생성 완료, channelId={}", channel.getId());
    return channelMapper.toDto(channel);
  }

  @Transactional(readOnly = true)
  @Override
  public ChannelDto find(UUID channelId) {
    return channelRepository.findById(channelId)
        .map(channelMapper::toDto)
        .orElseThrow(
            () -> new NoSuchElementException("Channel with id " + channelId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannel)
        .map(Channel::getId)
        .toList();

    return channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, mySubscribedChannelIds)
        .stream()
        .map(channelMapper::toDto)
        .toList();
  }

  /** 채널 수정 (공개 채널만, 비공개는 예외). */
  @Transactional
  @Override
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    String newName = request.newName();
    String newDescription = request.newDescription();
    log.debug("채널 수정, channelId={}", channelId);
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("채널 수정 실패: 채널 없음, channelId={}", channelId);
          return new NoSuchElementException("Channel with id " + channelId + " not found");
        });
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      log.warn("채널 수정 실패: 비공개 채널은 수정 불가, channelId={}", channelId);
      throw new IllegalArgumentException("Private channel cannot be updated");
    }
    channel.update(newName, newDescription);
    log.info("채널 수정 완료, channelId={}, name={}", channelId, newName);
    return channelMapper.toDto(channel);
  }

  /** 채널 삭제 (관련 메시지·읽음상태 함께 삭제). */
  @Transactional
  @Override
  public void delete(UUID channelId) {
    log.debug("채널 삭제 시도, channelId={}", channelId);
    if (!channelRepository.existsById(channelId)) {
      log.warn("채널 삭제 실패: 채널 없음, channelId={}", channelId);
      throw new NoSuchElementException("Channel with id " + channelId + " not found");
    }
    messageRepository.deleteAllByChannelId(channelId);
    readStatusRepository.deleteAllByChannelId(channelId);
    channelRepository.deleteById(channelId);
    log.info("채널 삭제 완료, channelId={}", channelId);
  }
}
