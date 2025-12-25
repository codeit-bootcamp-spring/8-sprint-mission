package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository; // ✅ 추가
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    //  다이어그램에 명시된 3개의 레포지토리 주입
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public ChannelResponse createPublic(ChannelCreateRequest request) {
        Channel channel = new Channel(
                request.getName(),
                request.getDescription(),
                ChannelType.PUBLIC,
                request.getOwnerId()
        );
        return convertToResponse(channelRepository.save(channel));
    }

    @Override
    public ChannelResponse create(ChannelCreateRequest request) {
        return null;
    }

    @Override
    public ChannelResponse createPrivate(ChannelCreateRequest request) {
        Channel channel = new Channel(
                request.getName(),
                request.getDescription(),
                ChannelType.PRIVATE,
                request.getOwnerId()
        );
        Channel savedChannel = channelRepository.save(channel);

        if (request.getMemberIds() != null) {
            for (UUID userId : request.getMemberIds()) {
                readStatusRepository.save(new ReadStatus(userId, savedChannel.getId(), null));
            }
        }
        return convertToResponse(savedChannel);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        Set<UUID> joinedChannelIds = readStatusRepository.findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .map(ReadStatus::getChannelId)
                .collect(Collectors.toSet());

        return channelRepository.findAll().stream()
                .filter(channel ->
                        channel.getType() == ChannelType.PUBLIC ||
                                (channel.getType() == ChannelType.PRIVATE && joinedChannelIds.contains(channel.getId()))
                )
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChannelResponse> findById(UUID id) {
        return channelRepository.findById(id).map(this::convertToResponse);
    }

    @Override
    public List<ChannelResponse> findAll() {
        return channelRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
        channel.update(request.getName(), request.getDescription());
        return convertToResponse(channelRepository.save(channel));
    }

    @Override
    public void delete(UUID id) {
        //  다이어그램 의도 반영: 채널 삭제 시 연관된 모든 데이터 연쇄 삭제

        // 1. 해당 채널의 모든 메시지 삭제
        messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(id))
                .forEach(m -> messageRepository.delete(m.getId()));

        // 2. 해당 채널의 모든 읽기 상태(ReadStatus) 삭제
        readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannelId().equals(id))
                .forEach(rs -> readStatusRepository.delete(rs.getId()));

        // 3. 채널 삭제
        channelRepository.delete(id);
    }

    private ChannelResponse convertToResponse(Channel channel) {
        Set<UUID> memberIds = readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channel.getId()))
                .map(ReadStatus::getUserId)
                .collect(Collectors.toSet());

        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .type(channel.getType())
                .ownerId(channel.getOwnerId())
                .memberIds(memberIds)
                .build();
    }
}