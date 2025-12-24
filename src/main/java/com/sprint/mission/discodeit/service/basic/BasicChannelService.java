package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public ChannelDto createPublicChannel(ChannelCreatePublicRequest request) {
        Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
        channelRepository.save(channel);
        return convertDto(channel);
    }

    @Override
    public ChannelDto createPrivateChannel(ChannelCreatePrivateRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepository.save(channel);

        if (request.memberUserIds() != null) {
            for (UUID userId : request.memberUserIds()) {
                ReadStatus rs = new ReadStatus(userId, channel.getId(), Instant.EPOCH);
                readStatusRepository.save(rs);
            }
        }
        return convertDto(channel);
    }

    @Override
    public ChannelDto findChannel(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Channel을 찾을 수 없습니다. " + id));
        return convertDto(channel);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(ch -> {
                    if (ch.getType() == ChannelType.PUBLIC) {
                        return true;
                    }
                    return readStatusRepository.findByUserIdAndChannelId(userId, ch.getId()).isPresent();
                })
                .map(channel -> this.convertDto(channel))
                .collect(Collectors.toList());
    }

    @Override
    public ChannelDto updateChannel(ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(request.id())
                .orElseThrow(() -> new NoSuchElementException("Channel을 찾을 수 없습니다. " + request.id()));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalStateException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(request.name(), request.description());
        channelRepository.save(channel);

        return convertDto(channel);
    }

    @Override
    public void deleteChannel(UUID id) {
        channelRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Channel을 찾을 수 없습니다. " + id));

        // 채널의 메시지와 첨부파일 삭제
        List<Message> messages = messageRepository.findAllByChannelId(id);
        for (Message m : messages) {
            if (m.getAttachmentIds() != null && !m.getAttachmentIds().isEmpty()) {
                binaryContentRepository.deleteAllByIdIn(m.getAttachmentIds());
            }
            messageRepository.delete(m.getId());
        }

        // ReadStatus 삭제
        readStatusRepository.deleteAllByChannelId(id);

        // 채널 삭제
        channelRepository.delete(id);
    }

    private ChannelDto convertDto(Channel channel) {
        // 최근 메시지 시각
        Instant lastMessageAt = messageRepository.findAllByChannelId(channel.getId()).stream()
                .map(message -> message.getCreatedAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

        // PRIVATE 채널 멤버
        List<UUID> memberUserIds = List.of();
        if (channel.getType() == ChannelType.PRIVATE) {
            memberUserIds = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                    .map(readStatus -> readStatus.getUserId())
                    .collect(Collectors.toList());
        }

        return new ChannelDto(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType().name(),
                lastMessageAt,
                memberUserIds
        );
    }
}