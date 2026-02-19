package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelExcption.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelExcption.PrivateChannelModificationNotAllowedException;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ReadStatusRepository readStatusRepository;

  private final ChannelMapper channelMapper;

  @Transactional
  @Override
  public ChannelDto create(PublicChannelCreateRequest request) {
    String name = request.name();
    String description = request.description();

    log.info("Service: Public 채널 생성 요청 - name: {}, description: {}", name, description);
    Channel channel = new Channel(name, description, ChannelType.PUBLIC);
    Channel savedChannel = channelRepository.save(channel);
    log.info("Service: Public 채널 생성 완료 - ID: {}", savedChannel.getId());

    return this.toDto(savedChannel);
  }

  @Transactional
  @Override
  public ChannelDto create(PrivateChannelCreateRequest request) {
    log.info("Service - Private 채널 생성 요청");
    Channel channel = new Channel(null, null, ChannelType.PRIVATE);
    Channel savedChannel = channelRepository.save(channel);

    List<ReadStatus> readStatuses = request.participantIds().stream()
        .map(userId -> {
          User user = userRepository.findById(userId)
              .orElseThrow(() -> {
                log.warn("Service: Private 채널 생성 실패(존재하지 않는 유저) - ID: {}", userId);
                return new UserNotFoundException(userId);
              });

          return new ReadStatus(user, channel, channel.getCreatedAt());
        })
        .toList();

    //성능 향상을 위해 하나씩 저장하는 것보다 한번에 저장하는 로직
    readStatusRepository.saveAll(readStatuses);

    log.info("Service - Private 채널 생성 완료 - ID: {}", savedChannel.getId());
    return this.toDto(savedChannel);
  }

  @Override
  public ChannelDto find(UUID channelId) {
    return channelRepository.findById(channelId)
        .map(this::toDto)
        .orElseThrow(() -> new ChannelNotFoundException(channelId));
  }

  @Override
  public List<ChannelDto> findAll(UUID userId) {
    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserIdWithChannel(userId)
        .stream()
        .map(ReadStatus::getChannel)
        .map(Channel::getId)
        .toList();

    return channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, mySubscribedChannelIds)
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ChannelDto update(UUID channelId, PublicChannelUpdateRequest request) {
    log.info("Service: Public 채널 수정 요청 - ID: {}", channelId);
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("Service: Public 채널 수정 실패(존재하지 않는 채널) - ID: {}", channelId);
          return new ChannelNotFoundException(channelId);
        });

    if (channel.getType().equals(ChannelType.PRIVATE)) {
      log.warn("Service: Private 채널은 수정할 수 없습니다. - ID: {}", channelId);
      throw new PrivateChannelModificationNotAllowedException(channelId);
    }

    channel.update(request.newName(), request.newDescription());

    log.info("Service: Public 채널 수정 완료 - ID: {}", channelId);
    return this.toDto(channelRepository.save(channel));
  }

  @Override
  @Transactional
  public void delete(UUID channelId) {
    log.info("Service: 채널 삭제 요청 - ID: {}", channelId);
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("Service: 채널 삭제 실패(존재하지 않는 채널) - ID: {}", channelId);
          return new ChannelNotFoundException(channelId);
        });

    channelRepository.delete(channel);
    log.info("Service: 채널 삭제 성공 - ID: {}", channelId);
  }

  private ChannelDto toDto(Channel channel) {
    Instant lastMessageAt = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
            channel.getId())
        .map(Message::getCreatedAt)
        .orElse(null);

    List<User> participants = new ArrayList<>();

    if (channel.getType().equals(ChannelType.PRIVATE)) {
      readStatusRepository.findAllByChannelIdWithUser(channel.getId()).stream()
          .map(ReadStatus::getUser)
          .forEach(participants::add);
    }

    return channelMapper.toDto(channel, participants, lastMessageAt);
  }
}
