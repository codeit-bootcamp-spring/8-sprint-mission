package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.exception.ChannelExcption.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelExcption.PrivateChannelModificationNotAllowedException;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @CacheEvict(value = "channels", allEntries = true)
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Transactional
    public ChannelDto create(PublicChannelCreateRequest request) {
        String name = request.name();
        String description = request.description();

        log.info("Service: Public 채널 생성 요청 - name: {}, description: {}", name, description);
        Channel channel = new Channel(name, description, ChannelType.PUBLIC);
        Channel savedChannel = channelRepository.save(channel);

        ChannelDto channelDto = this.toDto(savedChannel);

        eventPublisher.publishEvent(new ChannelCreatedEvent(channelDto, savedChannel.getCreatedAt()));

        log.info("Service: Public 채널 생성 완료 - ID: {}", savedChannel.getId());

        return channelDto;
    }

    @Override
    @CacheEvict(value = "channels", allEntries = true)
    @Transactional
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

        ChannelDto channelDto = this.toDto(savedChannel);

        eventPublisher.publishEvent(new ChannelCreatedEvent(channelDto, savedChannel.getCreatedAt()));

        log.info("Service - Private 채널 생성 완료 - ID: {}", savedChannel.getId());
        return channelDto;
    }

    @Override
    public ChannelDto find(UUID channelId) {
        return channelRepository.findById(channelId)
                .map(this::toDto)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));
    }

    @Override
    @Cacheable(value = "channels", key = "#userId")
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
    @CacheEvict(value = "channels", allEntries = true)
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
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

        ChannelDto prevChannel = this.toDto(channel);

        channel.update(request.newName(), request.newDescription());

        ChannelDto updatedChannelDto = this.toDto(channel);

        eventPublisher.publishEvent(new ChannelUpdatedEvent(
                prevChannel,
                updatedChannelDto,
                channel.getUpdatedAt())
        );

        log.info("Service: Public 채널 수정 완료 - ID: {}", channelId);
        return updatedChannelDto;
    }

    @Override
    @CacheEvict(value = "channels", allEntries = true)
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Transactional
    public void delete(UUID channelId) {
        ChannelDto channelDto = find(channelId);

        log.info("Service: 채널 삭제 요청 - ID: {}", channelId);
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    log.warn("Service: 채널 삭제 실패(존재하지 않는 채널) - ID: {}", channelId);
                    return new ChannelNotFoundException(channelId);
                });

        messageRepository.deleteAllByChannelId(channelId);
        readStatusRepository.deleteAllByChannelId(channelId);

        channelRepository.delete(channel);

        eventPublisher.publishEvent(new ChannelDeletedEvent(channelDto, Instant.now()));

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
