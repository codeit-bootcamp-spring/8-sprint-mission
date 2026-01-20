package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
        // 채널 이름이 없으면 기본값 설정
        String channelName = request.getName();
        if (channelName == null || channelName.trim().isEmpty()) {
            channelName = "개인 메시지";
        }
        
        Channel channel = new Channel(
                channelName,
                request.getDescription(),
                com.sprint.mission.discodeit.entity.ChannelType.PRIVATE,
                request.getOwnerId()
        );
        Channel savedChannel = channelRepository.save(channel);
        
        // 모든 참여자(participantIds + ownerId)에 대해 ReadStatus 생성
        Set<UUID> allParticipantIds = new HashSet<>();
        
        // participantIds 추가
        if (request.getParticipantIds() != null) {
            allParticipantIds.addAll(request.getParticipantIds());
        }
        
        // ownerId도 참여자로 추가 (없으면 제외)
        if (request.getOwnerId() != null) {
            allParticipantIds.add(request.getOwnerId());
        }
        
        // 각 참여자에 대해 ReadStatus 생성 (채널 생성 시간을 lastReadAt으로 설정)
        allParticipantIds.stream()
                .map(userId -> new ReadStatus(userId, savedChannel.getId(), savedChannel.getCreatedAt()))
                .forEach(readStatusRepository::save);
        
        return convertToResponse(savedChannel);
    }

    @Override
    public Optional<ChannelResponse> findById(UUID id) {
        // 기본 구현: 권한 체크 없이 조회 (기존 호환성 유지)
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널을 찾을 수 없습니다."));
        return Optional.of(convertToResponse(channel));
    }
    
    /**
     * 채널 조회 (권한 체크 포함)
     * 개인 채널인 경우 사용자가 참여자인지 확인
     */
    public Optional<ChannelResponse> findById(UUID id, UUID userId) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널을 찾을 수 없습니다."));
        
        // 개인 채널인 경우 사용자가 참여자인지 확인
        if (channel.getType() == ChannelType.PRIVATE) {
            boolean hasAccess = readStatusRepository.findAllByUserId(userId).stream()
                    .anyMatch(readStatus -> readStatus.getChannelId().equals(id));
            
            if (!hasAccess) {
                throw new IllegalArgumentException("이 채널에 접근할 권한이 없습니다.");
            }
        }
        
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
        // ReadStatus에서 실제 참여자 ID 조회 (PRIVATE 채널의 경우)
        Set<UUID> memberIds;
        if (channel.getType() == ChannelType.PRIVATE) {
            memberIds = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                    .map(ReadStatus::getUserId)
                    .collect(Collectors.toSet());
        } else {
            // PUBLIC 채널은 memberIds가 없거나 빈 Set
            memberIds = channel.getMemberIds() != null ? channel.getMemberIds() : new HashSet<>();
        }
        
        return new ChannelResponse(
                channel.getId(),            // 1. UUID id
                channel.getName(),          // 2. String name
                channel.getDescription(),   // 3. String description
                channel.getType(),          // 4. ChannelType type
                channel.getOwnerId(),       // 5. UUID ownerId
                memberIds                   // 6. Set<UUID> memberIds (실제 참여자 ID)
        );
    }
}