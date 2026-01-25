package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelDto.ChannelDtoBuilder;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

  private final MessageRepository messageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserMapper userMapper;

  public ChannelDto toDto(Channel channel) {
    if (channel == null) {
      return null;
    }

    // 참여자 목록 조회 (ReadStatus에서)
    List<UserDto> participants = null;
    if (channel.getType() == ChannelType.PRIVATE) {
      // PRIVATE 채널의 경우 ReadStatus에서 참여자 조회
      List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelId(channel.getId());
      if (readStatuses != null && !readStatuses.isEmpty()) {
        // ReadStatus에서 User 엔티티를 가져와서 변환
        participants = readStatuses.stream()
            .map(ReadStatus::getUser)
            .filter(user -> user != null)
            .map(userMapper::toDto)
            .distinct()
            .collect(Collectors.toList());
      }
    } else {
      // PUBLIC 채널은 참여자 목록이 없거나 빈 리스트
      participants = List.of();
    }

    // 마지막 메시지 시간 조회
    AtomicReference<Object> lastMessageAt = new AtomicReference<>();
    messageRepository.findTopByChannelIdOrderByCreatedAtDesc(channel.getId())
        .ifPresent(message -> lastMessageAt.set(message.getCreatedAt()));

    return ChannelDto.builder()
        .id(channel.getId())
        .type(channel.getType())
        .name(channel.getName())
        .description(channel.getDescription())
        .participants(participants).build();
  }

  private ChannelDtoBuilder lastMessageAt(Instant instant) {
    return null;
  }
}
