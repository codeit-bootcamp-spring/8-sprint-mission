package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

public class JCFChannelService implements ChannelService {
    private final List<Channel> data;
    private final ChannelRepository channelRepository;
    private final UserService userService;

    public JCFChannelService(ChannelRepository channelRepository, UserService userService) {
        this.channelRepository = channelRepository;
        this.userService = userService;
        this.data = new ArrayList<>();
    }

    @Override
    public void createChannel(Channel channel) {
        channel.setModifiedAt(channel.getCreatedAt());
        data.add(channel);

    }

    @Override
    public Channel create(UUID uid, String name, String host, int participant, List<String> participants) {
        User user = userService.findById(uid);
        if(user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        Channel channel = new Channel(uid, name, user.getName(), participant, participants);
        channel.setModifiedAt(channel.getCreatedAt());
        data.add(channel);
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        return data.stream()
                .filter(channel -> channel.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Channel> findChannels() {
        return data;
    }

    @Override
    public Map<String, List<Channel>> findChannelByName() {
        return data.stream()
                .collect(
                        Collectors.groupingBy(
                                Channel::getName,
                                Collectors.toList()
                        ));
    }

    @Override
    public List<Channel> findChannelByTopNParticipant(int n) {
        return data.stream()
                .limit(n)
                .sorted((c1, c2) -> c2.getParticipant() - c1.getParticipant())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findChannelByParticipantsASC(UUID id) {
        return data.stream()
                .filter(channel -> channel.getId().equals(id))
                .findFirst()
                .map(Channel::getParticipants)
                .map(participants -> {
                    List<String> sortedList = new ArrayList<>(participants);
                    Collections.sort(sortedList);
                    return sortedList;
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public void updateChannel(UUID id, String fieldName, String value) {
        data.stream()
                .filter(channel -> channel.getId().equals(id))
                .forEach(channel -> {
                    if(fieldName.equals("name")) {
                        channel.setName(value);
                        channel.setModifiedAt(System.currentTimeMillis());
                    } else if(fieldName.equals("host")) {
                        channel.setHost(value);
                        channel.setModifiedAt(System.currentTimeMillis());
                    }
                });
    }

    @Override
    public void deleteChannel(UUID id) {
        Iterator<Channel> iterator = data.iterator();
        while (iterator.hasNext()) {
            Channel channel = iterator.next();
            if(channel.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public Optional<Channel> findByCId(UUID id) {
        return Optional.empty();
    }

    @Override
    public void saveChannel(Channel channel) {
        channel.setModifiedAt(channel.getCreatedAt());
        data.add(channelRepository.save(channel));
    }
}
