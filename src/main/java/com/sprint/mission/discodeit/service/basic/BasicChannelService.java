package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
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

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository; // 연관 데이터 삭제를 위해 주입
    private final ReadStatusRepository readStatusRepository; // 사용자별 채널 조회를 위해 주입

    @Override
    public ChannelResponse create(String name, String description) {
        // 기본적으로 PUBLIC 채널로 생성 (기존 호환성 유지)
        Channel channel = new Channel(name, description, com.sprint.mission.discodeit.entity.ChannelType.PUBLIC, null);
        channelRepository.save(channel);
        return convertToResponse(channel);
    }

    @Override
    public List<ChannelResponse> findAll() {
        return channelRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        // 1. 모든 PUBLIC 채널 조회 (모든 사용자가 볼 수 있음)
        List<Channel> publicChannels = channelRepository.findAll().stream()
                .filter(channel -> channel.getType() == ChannelType.PUBLIC)
                .collect(Collectors.toList());

        // 2. 해당 사용자가 ReadStatus에 등록된 PRIVATE 채널 ID 조회
        Set<UUID> privateChannelIds = readStatusRepository.findAllByUserId(userId).stream()
                .map(readStatus -> readStatus.getChannelId())
                .collect(Collectors.toSet());

        // 3. 해당 사용자가 접근 가능한 PRIVATE 채널 조회
        List<Channel> privateChannels = channelRepository.findAll().stream()
                .filter(channel -> channel.getType() == ChannelType.PRIVATE
                        && privateChannelIds.contains(channel.getId()))
                .collect(Collectors.toList());

        // 4. PUBLIC 채널과 PRIVATE 채널을 합쳐서 DTO로 변환하여 반환
        List<Channel> allVisibleChannels = new java.util.ArrayList<>(publicChannels);
        allVisibleChannels.addAll(privateChannels);

        return allVisibleChannels.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        return null;
    }

    @Override
    public ChannelResponse createPublic(ChannelCreateRequest request) {
        Channel channel = new Channel(
                request.getName(),
                request.getDescription(),
                com.sprint.mission.discodeit.entity.ChannelType.PUBLIC,
                request.getOwnerId()
        );
        Channel savedChannel = channelRepository.save(channel);
        return convertToResponse(savedChannel);
    }

    @Override
    public ChannelResponse create(ChannelCreateRequest request) {
        // 기본적으로 PUBLIC 채널로 생성
        return createPublic(request);
    }

    @Override
    public ChannelResponse createPrivate(ChannelCreateRequest request) {
        Channel channel = new Channel(
                request.getName(),
                request.getDescription(),
                com.sprint.mission.discodeit.entity.ChannelType.PRIVATE,
                request.getOwnerId()
        );
        Channel savedChannel = channelRepository.save(channel);
        return convertToResponse(savedChannel);
    }

    @Override
    public Optional<ChannelResponse> findById(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널을 찾을 수 없습니다."));
        return Optional.of(convertToResponse(channel));
    }

    @Override
    public ChannelResponse update(UUID id, String name, String description) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널을 찾을 수 없습니다."));
        channel.update(name, description);
        channelRepository.save(channel);
        return convertToResponse(channel);
    }

    /**
     * 채널 삭제: 연관된 메시지를 먼저 효율적으로 삭제한 후 채널을 삭제합니다.
     */
    @Override
    public void delete(UUID id) {
        // [멘토 피드백 반영] 비효율적인 findAll() 스트림 대신 전용 삭제 메서드 호출
        // 이 한 줄의 위임이 데이터가 많아질수록 성능 차이를 극명하게 만듭니다.
        messageRepository.deleteByChannelId(id);

        // 채널 자체 삭제
        channelRepository.delete(id);
    }

    private ChannelResponse convertToResponse(Channel channel) {
        return new ChannelResponse(
                channel.getId(),            // 1. UUID id
                channel.getName(),          // 2. String name
                channel.getDescription(),   // 3. String description
                channel.getType(),          // 4. ChannelType type (엔티티에 해당 필드가 있어야 함)
                channel.getOwnerId(),       // 5. UUID ownerId (엔티티에 해당 필드가 있어야 함)
                channel.getMemberIds()      // 6. Set<UUID> memberIds (엔티티에 해당 필드가 있어야 함)
        );
    }
}