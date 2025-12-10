package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.Optional;
import java.util.UUID;

public class JCFChannelRepository implements ChannelRepository {
    @Override
    public Channel save(Channel channel) {
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.empty();
    }
}
