package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.util.*;
import java.util.stream.Collectors;

public class JCFChannelRepository implements ChannelRepository {

    private static JCFChannelRepository INSTANCE;
    private final Map<UUID, Channel> data;

    private JCFChannelRepository() {
        this.data = new HashMap<>();
    }

    public static JCFChannelRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JCFChannelRepository();
        }
        return INSTANCE;
    }

    @Override
    public Channel save(Channel channel) {
        data.put(channel.getId(), channel);
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Channel> findAll() {
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}