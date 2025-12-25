package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
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
    private final ReadStatusRepository readStatusRepository;

    @Override
    public ChannelResponse createPublic(ChannelCreateRequest request) {
        // PUBLIC 채널 생성
        Channel channel = new Channel(
                request.getName(),
                request.getDescription(),
                ChannelType.PUBLIC,
                request.getOwnerId()
        );
        Channel savedChannel = channelRepository.save(channel);
        return convertToResponse(savedChannel);
    }

    @Override
    public ChannelResponse create(ChannelCreateRequest request) {
        return null;
    }

    @Override
    public ChannelResponse createPrivate(ChannelCreateRequest request) {
        // 1. PRIVATE 채널 생성
        Channel channel = new Channel(
                request.getName(),
                request.getDescription(),
                ChannelType.PRIVATE,
                request.getOwnerId()
        );
        Channel savedChannel = channelRepository.save(channel);

        // 2. [멘토 피드백 반영] Channel 엔티티에 멤버를 저장하는 대신 ReadStatus를 생성하여 관계 정립
        if (request.getMemberIds() != null) {
            for (UUID userId : request.getMemberIds()) {
                ReadStatus readStatus = new ReadStatus(userId, savedChannel.getId(), null);
                readStatusRepository.save(readStatus);
            }
        }

        return convertToResponse(savedChannel);
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
        // 특정 유저가 속한 채널 리스트 조회 (ReadStatus 기준)
        Set<UUID> joinedChannelIds = readStatusRepository.findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .map(ReadStatus::getChannelId)
                .collect(Collectors.toSet());

        return channelRepository.findAll().stream()
                .filter(channel -> joinedChannelIds.contains(channel.getId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        channel.update(request.getName(), request.getDescription());
        Channel updated = channelRepository.save(channel);
        return convertToResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        // 채널 삭제 시 연관된 ReadStatus 모두 삭제 (중복 관리 제거 후 관계 정리 핵심)
        readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannelId().equals(id))
                .forEach(rs -> readStatusRepository.delete(rs.getId()));

        channelRepository.delete(id);
    }

    private ChannelResponse convertToResponse(Channel channel) {
        // [중요] ChannelResponse에 필요한 memberIds는 ReadStatusRepository를 통해 실시간으로 취합
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
                .memberIds(memberIds) // 엔티티가 아닌 Repository 조회를 통해 데이터 구성
                .build();
    }
}