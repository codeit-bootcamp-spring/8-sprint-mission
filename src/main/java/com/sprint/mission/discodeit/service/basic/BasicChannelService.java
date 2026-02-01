package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
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
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final MessageRepository messageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelMapper channelMapper;

  @Override
  @Transactional
  public ChannelDto createPublicChannel(ChannelCreatePublicRequest request) {
    Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
    Channel savedChannel = channelRepository.save(channel);
    return channelMapper.toDto(savedChannel);
  }

  @Override
  @Transactional
  public ChannelDto createPrivateChannel(ChannelCreatePrivateRequest request) {
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    Channel savedChannel = channelRepository.save(channel);

    // 참여자 (ReadStatus) 생성
    if (request.participantIds() != null) {
      for (UUID userId : request.participantIds()) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다: " + userId));

        ReadStatus rs = new ReadStatus(user, savedChannel, Instant.EPOCH);
        readStatusRepository.save(rs);
      }
    }
    return channelMapper.toDto(savedChannel);
  }

  @Override
  public ChannelDto findChannel(UUID id) {
    return channelRepository.findById(id)
        .map(channelMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException("Channel을 찾을 수 없습니다. " + id));
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    return channelRepository.findAllAccessibleByUserId(userId).stream()
        .map(channelMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ChannelDto updateChannel(UUID channelId, ChannelUpdateRequest request) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException("Channel을 찾을 수 없습니다. " + channelId));

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalStateException("PRIVATE 채널은 수정할 수 없습니다.");
    }

    channel.update(request.newName(), request.newDescription());

    return channelMapper.toDto(channel);
  }

  @Override
  @Transactional
  public void deleteChannel(UUID id) {
    if (!channelRepository.existsById(id)) {
      throw new NoSuchElementException("Channel을 찾을 수 없습니다. " + id);
    }

    // 채널의 메시지와 첨부파일 삭제
    messageRepository.findAllByChannel_Id(id).forEach(message -> {
      messageRepository.delete(message);
    });

    // ReadStatus 삭제
    readStatusRepository.deleteAllByChannel_Id(id);

    // 채널 삭제
    channelRepository.deleteById(id);
  }
}