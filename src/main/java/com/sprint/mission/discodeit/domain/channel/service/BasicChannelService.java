package com.sprint.mission.discodeit.domain.channel.service;

import com.sprint.mission.discodeit.domain.channel.domain.Channel;
import com.sprint.mission.discodeit.domain.channel.domain.enums.ChannelType;
import com.sprint.mission.discodeit.domain.channel.dto.domain.ChannelDto;
import com.sprint.mission.discodeit.domain.channel.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.domain.channel.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.domain.channel.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.domain.channel.event.ChannelEvent;
import com.sprint.mission.discodeit.domain.channel.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.domain.channel.exception.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.domain.channel.mapper.ChannelMapper;
import com.sprint.mission.discodeit.domain.channel.repository.ChannelRepository;
import com.sprint.mission.discodeit.domain.message.repository.MessageRepository;
import com.sprint.mission.discodeit.domain.readstatus.domain.ReadStatus;
import com.sprint.mission.discodeit.domain.readstatus.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.domain.user.exception.UserNotFoundException;
import com.sprint.mission.discodeit.domain.user.mapper.UserMapper;
import com.sprint.mission.discodeit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService
{
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @CacheEvict(value = "channels", allEntries = true)
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Transactional
    @Override
    public ChannelDto create(PublicChannelCreateRequest publicChannelCreateRequest) {
        log.debug("채널 생성 처리 시작(공개): name={}, description={}", publicChannelCreateRequest.name(), publicChannelCreateRequest.description());

        Channel channel = new Channel(
                publicChannelCreateRequest.name(),
                publicChannelCreateRequest.description(),
                ChannelType.PUBLIC
        );

        channelRepository.save(channel);

        ChannelDto channelDto = channelMapper.toDto(channel);
        applicationEventPublisher.publishEvent(new ChannelEvent("channels.created", channelDto, channel.getCreatedAt()));

        log.info("채널 생성 처리 완료(공개): channelId={}", channel.getId());

        return channelDto;
    }

    @CacheEvict(value = "channels", allEntries = true)
    @Transactional
    @Override
    public ChannelDto create(PrivateChannelCreateRequest privateChannelCreateRequest) {
        log.debug("채널 생성 처리 시작(비공개): participants={}", privateChannelCreateRequest.participantIds().size());

        Channel channel = new Channel(null, null, ChannelType.PRIVATE);

        privateChannelCreateRequest.participantIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> {
                            log.warn("존재하지 않는 사용자: userId={}", userId);
                            return new UserNotFoundException(userId);
                        }))
                .map(user -> new ReadStatus(user, channel, channel.getCreatedAt(), true))
                .forEach(channel.getReadStatuses()::add);

        channelRepository.save(channel);

        ChannelDto channelDto = channelMapper.toDto(channel);
        applicationEventPublisher.publishEvent(new ChannelEvent("channels.created", channelDto, channel.getCreatedAt()));

        log.info("채널 생성 처리 완료(비공개): channelId={}", channel.getId());

        return channelDto;
    }

    @Transactional(readOnly = true)
    @Override
    public ChannelDto find(UUID channelId) {
        return channelRepository.findById(channelId)
                .map(channelMapper::toDto)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));
    }

    @Cacheable(value = "channels", key = "#userId")
    @Transactional(readOnly = true)
    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        return channelRepository.findAllAccessibleByUserId(userId).stream()
                .map(channelMapper::toDto)
                .toList();
    }

    @CacheEvict(value = "channels", allEntries = true)
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Transactional
    @Override
    public ChannelDto update(UUID channelId, PublicChannelUpdateRequest publicChannelUpdateRequest) {
        log.debug("채널 수정 처리 시작: channelId={}, name={}, description={}", channelId, publicChannelUpdateRequest.newName(), publicChannelUpdateRequest.newDescription());

       Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));

       if (channel.getType().equals(ChannelType.PRIVATE)) {
           log.warn("비공개 채널 수정 시도: channnelId={}", channelId);
           throw new ChannelUpdateNotAllowedException(channelId);
       }

       channel.update(publicChannelUpdateRequest.newName(), publicChannelUpdateRequest.newDescription());

        ChannelDto channelDto = channelMapper.toDto(channel);
        applicationEventPublisher.publishEvent(new ChannelEvent("channels.updated", channelDto, channel.getCreatedAt()));

       log.info("채널 수정 처리 완료: channelId={}", channel.getId());

       return channelDto;
    }

    @CacheEvict(value = "channels", allEntries = true)
    @PreAuthorize("hasRole('CHANNEL_MANAGER')")
    @Transactional
    @Override
    public void delete(UUID channelId) {
        log.debug("채널 삭제 처리 시작:  channelId={}", channelId);

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));

        ChannelDto channelDto = channelMapper.toDto(channel);
        applicationEventPublisher.publishEvent(new ChannelEvent("channels.deleted", channelDto, channel.getCreatedAt()));

        channelRepository.delete(channel);

        log.info("채널 삭제 처리 완료: channelId={}", channel.getId());
    }
}
