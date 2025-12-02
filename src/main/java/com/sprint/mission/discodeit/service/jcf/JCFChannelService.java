package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> data = new HashMap<>();

    // 채널 생성
    @Override
    public Channel createChannel(String name, String description) {
        Channel channel = new Channel(name, description);
        data.put(channel.getId(), channel);
        return channel;
    }

    // 채널 조회 (단건)
    @Override
    public Channel findChannel(UUID id) {
        return data.get(id);
    }

    // 채널 조회 (다건)
    @Override
    public List<Channel> findAllChannels() {
        return new ArrayList<>(data.values());
    }

    // 채널 수정
    @Override
    public Channel updateChannel(UUID id, String channelName, String description) {
        Channel channel = data.get(id);

        if (channel == null) {
            throw new IllegalArgumentException("해당 채널이 존재하지 않습니다.");
        }

        channel.update(channelName, description);

        return channel;
    }

    // 채널 삭제
    @Override
    public void deleteChannel(UUID id) {
        data.remove(id);
    }


}
