package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    @Override
    public Channel create(String name, UUID ownerId) {
        Channel channel = new Channel(name, ownerId);
        return channelRepository.save(channel);
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public Channel update(UUID id, String name, UUID ownerId) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + id));
        channel.update(name, ownerId);
        return channelRepository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }
}