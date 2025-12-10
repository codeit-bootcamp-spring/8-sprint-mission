package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;

    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public void createChannel(Channel channel) {
    }

    @Override
    public Channel create(UUID uid, String name, String host, int participant, List<String> participants) {
        Channel channel = new Channel(uid, name, host, participant, participants);
        channelRepository.save(channel);
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        return null;
    }

    @Override
    public List<Channel> findChannels() {
        return List.of();
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
    public void updateChannel(UUID id, String fieldName, String value) {

    }

    @Override
    public void deleteChannel(UUID id) {

    }

    @Override
    public Optional<Channel> findByCId(UUID id) {
        return Optional.empty();
    }

    @Override
    public void saveChannel(Channel channel) {

    }
}
