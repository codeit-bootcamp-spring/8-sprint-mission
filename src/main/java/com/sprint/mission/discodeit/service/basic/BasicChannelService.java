package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    public ChannelResponse createPublic(ChannelCreateRequest request) {
        userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 소유자 ID입니다."));

        Channel channel = new Channel(request.getName(), "PUBLIC", request.getOwnerId());
        Channel saved = channelRepository.save(channel);
        return convertToResponse(saved);
    }

    @Override
    public ChannelResponse createPrivate(ChannelCreateRequest request) {
        userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 소유자 ID입니다."));

        Channel channel = new Channel(request.getName(), "PRIVATE", request.getOwnerId());
        if (request.getMemberIds() != null) {
            request.getMemberIds().forEach(channel::addMember);
        }
        Channel saved = channelRepository.save(channel);
        return convertToResponse(saved);
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
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(channel -> channel.getMemberIds().contains(userId) || channel.getOwnerId().equals(userId))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        channel.update(request.getName());
        Channel updated = channelRepository.save(channel);
        return convertToResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }

    private ChannelResponse convertToResponse(Channel channel) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .type(channel.getType())
                .ownerId(channel.getOwnerId())   // 이제 DTO에 필드가 있으므로 에러가 나지 않습니다.
                .memberIds(channel.getMemberIds())
                .build();
    }
}