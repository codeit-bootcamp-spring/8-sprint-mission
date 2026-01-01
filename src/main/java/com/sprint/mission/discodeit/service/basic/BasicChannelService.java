package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.DTO.request.ChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.response.ChannelResponse;
import com.sprint.mission.discodeit.DTO.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.Exception.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public ChannelResponse createPublic(ChannelCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));

        Channel channel = new Channel(
                request.userId(),
                request.status(),
                request.name(),
                user.getName(),
                request.description(),
                null,
                null
        );

        Channel savedChannel = channelRepository.save(channel);

        return ChannelResponse.from(savedChannel, null, List.of());
    }

    @Override
    public ChannelResponse createPrivate(ChannelCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));

        List<String> participants = new ArrayList<>();
        participants.add(user.getName());

        Channel channel = new Channel(
                request.userId(),
                request.status(),
                null,
                user.getName(),
                null,
                participants.size(),
                participants
        );

        Channel savedChannel = channelRepository.save(channel);

        request.participantsIds().stream()
                .map(userId -> new ReadStatus(userId, savedChannel.getId(), Instant.MIN))
                .forEach(readStatusRepository::save);

        return ChannelResponse.from(savedChannel, Instant.MIN, List.of());
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
    public List<ChannelResponse> findAll(UUID userId) {
        //PUBLIC 채널 전체 조회
        List<Channel> publicChannels = channelRepository.findAllByStatus(ChannelStatus.PUBLIC);
        //PRIVATE 채널 중 유저가 참여한 채널만 조회
        List<Channel> privateChannels = channelRepository.findAllPrivateChannelIdsByUserId(userId);

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
    public Map<String, List<Channel>> findChannelByChannelName() {
        return channelRepository.findAll().stream()
                .collect(
                        Collectors.groupingBy(
                                Channel::getName,
                                Collectors.toList()
                        )
                );
    }

    @Override
    public List<Channel> findChannelByTopNParticipant(int n) {
        return channelRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getParticipant() - c1.getParticipant())
                .limit(n)
                .toList();
    }

    @Override
    public List<String> findChannelByParticipantsASC(UUID id) {
        return channelRepository.findAll().stream()
                .filter(channel -> channel.getId().equals(id))
                .map(Channel::getParticipants)
                .flatMap(List::stream)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(UUID id, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("수정하려는 채널이 없습니다."));

        if(channel.getStatus().equals(ChannelStatus.PRIVATE)) {
            throw new ChannelUpdateNotAllowedException();
        }

        channel.update(request.channelName(), request.description());

        channelRepository.save(channel);

        return ChannelResponse.from(channel,  null, List.of());
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 채널이 없습니다. "));

        //채널과 관련된 메시지 전부 삭제
        messageRepository.deleteAllByChannelId(channel.getId());

        //채널과 관련된 readStatus 전부 삭제
        readStatusRepository.deleteAllByChannelId(channel.getId());

        //채널 삭제
        channelRepository.delete(id);

    }
}
