package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.DTO.request.ChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.response.ChannelResponse;
import com.sprint.mission.discodeit.DTO.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.Exception.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public ChannelResponse createPublic(ChannelCreateRequest request) {
        if (userRepository.findById(request.uId()).isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 유저입니다.");
        }

        Channel channel = new Channel(
                request.uId(),
                request.status(),
                request.name(),
                request.host(),
                request.description(),
                request.participant(),
                request.participants()
        );

        Channel savedChannel = channelRepository.save(channel);

        return ChannelResponse.from(savedChannel, null, List.of());
    }

    @Override
    public ChannelResponse createPrivate(ChannelCreateRequest request) {
        if (userRepository.findById(request.uId()).isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 유저입니다.");
        }

        Channel channel = new Channel(
                request.uId(),
                request.status(),
                null,
                request.host(),
                null,
                request.participant(),
                request.participants()
        );

        Channel savedChannel = channelRepository.save(channel);

        request.participantsIds().stream()
                .map(userId -> new ReadStatus(userId, savedChannel.getId(), Instant.MIN))
                .forEach(readStatusRepository::save);

        return ChannelResponse.from(savedChannel, null, List.of());
    }
    @Override
    public ChannelResponse findById(UUID id) {
        Channel channel =  channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 채널이 없습니다."));

        //채널이 생성했을 때 메시시가 없을 경우도 포함, 메시지가 최근에 생성된 것 기준으로 시간을 조회
        Instant latestMessageAt = messageRepository.findLatestMessageTimeByChannelId(channel.getId())
                .orElse(null);



        List<UUID> participantUserIds = List.of();

        if(channel.getStatus().equals(ChannelStatus.PUBLIC)) {
            return ChannelResponse.from(channel, latestMessageAt, participantUserIds);
        } else {
            participantUserIds = readStatusRepository.findAllByChannelId(channel.getId());
            return ChannelResponse.from(channel, latestMessageAt, participantUserIds);
        }
    }

    @Override
    public List<ChannelResponse> findAll(UUID uId) {
        //PUBLIC 채널 전체 조회
        List<Channel> publicChannels = channelRepository.findAllByStatus(ChannelStatus.PUBLIC);
        //PRIVATE 채널 중 유저가 참여한 채널만 조회
        List<Channel> privateChannels = channelRepository.findAllPrivateChannelIdsByUserId(uId);

        Set<Channel> allChannels = new HashSet<>();
        allChannels.addAll(publicChannels);
        allChannels.addAll(privateChannels);

        List<ChannelResponse> result = new ArrayList<>();
        for (Channel channel : allChannels) {
            Instant latestMessageAt = messageRepository.findLatestMessageTimeByChannelId(channel.getId())
                    .orElse(null);

            List<UUID> participantUserIds = null;

            if(channel.getStatus().equals(ChannelStatus.PRIVATE)) {
                participantUserIds = readStatusRepository.findAllByChannelId(channel.getId());
            }
            result.add(
                    ChannelResponse.from(
                            channel,
                            latestMessageAt,
                            participantUserIds
                    )
            );
        }
        return result;
    }

    @Override
    public Map<String, List<Channel>> findChannelByName() {
        return Map.of();
    }

    @Override
    public List<Channel> findChannelByTopNParticipant(int n) {
        return List.of();
    }

    @Override
    public List<String> findChannelByParticipantsASC(UUID id) {
        return List.of();
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(request.id())
                .orElseThrow(() -> new NoSuchElementException("수정하려는 채널이 없습니다."));
        if(channel.getStatus().equals(ChannelStatus.PRIVATE)) {
            throw new ChannelUpdateNotAllowedException();
        }
        channel.update(request.name(), request.description(), request.participant(), request.participants());

        channelRepository.save(channel);

        return ChannelResponse.from(channel,  null, List.of());
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 채널이 없습니다. "));

        messageRepository.deleteAllByChannelId(channel.getId());

        readStatusRepository.deleteAllByChannelId(channel.getId());

        channelRepository.delete(id);

    }
}
