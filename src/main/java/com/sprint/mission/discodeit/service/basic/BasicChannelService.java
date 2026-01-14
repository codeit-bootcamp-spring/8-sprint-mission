package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final MessageRepository messageRepository;
  private final ReadStatusRepository readStatusRepository;

  @Override
  public ChannelResponse create(PublicChannelCreateRequest request) {
    String name = request.name();
    String description = request.description();
    Channel channel = new Channel(ChannelType.PUBLIC, name, description);
    Channel savedChannel = channelRepository.save(channel);

    return ChannelResponse.from(savedChannel, null, List.of());
  }

  @Override
  public ChannelResponse create(PrivateChannelCreateRequest request) {
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel savedChannel = channelRepository.save(channel);

    request.participantIds().stream()
        .map(userId -> new ReadStatus(userId, savedChannel.getId(), Instant.MIN))
        .forEach(readStatusRepository::save);

    return ChannelResponse.from(savedChannel, Instant.MIN, request.participantIds());
  }

  @Override
  public ChannelResponse find(UUID channelId) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException("해당하는 채널이 없습니다."));
    return convertToChannelResponse(channel);
  }

  @Override
  public List<ChannelResponse> findAll(UUID userId) {
    List<UUID> mySubscribedChannelIds = readStatusRepository.findAllByUserId(userId).stream()
        .map(ReadStatus::getChannelId)
        .toList();
    return channelRepository.findAll().stream()
        .filter(channel ->
            channel.getType().equals(ChannelType.PUBLIC) ||
                mySubscribedChannelIds.contains(channel.getId())
        )
        .map(this::convertToChannelResponse)
        .toList();
  }

  @Override
  public ChannelResponse update(UUID channelId, PublicChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException("수정하려는 채널이 없습니다."));

    if (channel.getType().equals(ChannelType.PRIVATE)) {
      throw new IllegalArgumentException("개인 채널은 수정할 수 없습니다.");
    }

    channel.update(request.newName(), request.newDescription());

    channelRepository.save(channel);

    return ChannelResponse.from(channel, null,
        convertToChannelResponse(channel).participantIds());
  }

  @Override
  public void delete(UUID id) {
    Channel channel = channelRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("삭제하려는 채널이 없습니다. "));

    //채널과 관련된 메시지 전부 삭제
    messageRepository.deleteAllByChannelId(channel.getId());

    //채널과 관련된 readStatus 전부 삭제
    readStatusRepository.deleteAllByChannelId(channel.getId());

    //채널 삭제
    channelRepository.delete(id);
  }

  private ChannelResponse convertToChannelResponse(Channel channel) {
    // 1. 최신 메시지 시간 조회
    Instant lastMessageAt = messageRepository.findLatestMessageTimeByChannelId(channel.getId())
        .orElse(null);

    // 2. 참여자 목록 조회 (Private인 경우만, Public은 빈 리스트)
    List<UUID> participantUserIds = List.of();
    if (channel.getType().equals(ChannelType.PRIVATE)) {
      participantUserIds = readStatusRepository.findAllByChannelId(channel.getId()).stream()
          .map(ReadStatus::getUserId)
          .toList();
    }

    return ChannelResponse.from(channel, lastMessageAt, participantUserIds);
  }
}
