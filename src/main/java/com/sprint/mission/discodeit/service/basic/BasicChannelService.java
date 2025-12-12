package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    // AppConfig에서 주입 (DI)
    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public Channel createChannel(String name, String description) {
        Channel channel = new Channel(name, description);

        return channelRepository.save(channel);
    }

    @Override
    public Channel findChannel(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> findAllChannels() {
        return channelRepository.findAll();
    }

    @Override
    public Channel updateChannel(UUID id, String name, String description) {
        // 기존 채널 조회
        Channel channel = channelRepository.findById(id);

        if (channel == null) throw new IllegalArgumentException("해당 채널이 존재하지 않습니다.");

        channel.update(name, description);

        return channelRepository.update(id, channel);
    }

    @Override
    public void deleteChannel(UUID id) {
        channelRepository.delete(id);
    }
}