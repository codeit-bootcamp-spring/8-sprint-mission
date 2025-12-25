package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JCFChannelRepository implements ChannelRepository {
    private final List<Channel> channels = new ArrayList<>();

    @Override
    public Channel save(Channel channel) {
        // 기존에 같은 ID가 있으면 업데이트, 없으면 추가
        channels.removeIf(c -> c.getId().equals(channel.getId()));
        channels.add(channel);
        return channel; //  이 return이 없거나 null이면 Service에서 null을 받게 됨
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return channels.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(channels);
    }

    @Override
    public void delete(UUID id) {
        channels.removeIf(c -> c.getId().equals(id));
    }
}