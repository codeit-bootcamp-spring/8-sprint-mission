package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository; // 추가됨

@Repository // 1. 이 클래스를 Spring Bean(Repository)으로 등록합니다.
public class FileChannelRepository implements ChannelRepository {

    // 2. 이제 Spring이 싱글톤으로 관리해주므로,
    // 기존의 static instance와 getInstance() 메서드는 더 이상 필요하지 않습니다.
    private final Map<UUID, Channel> data = new HashMap<>();

    // 기본 생성자를 통해 Spring이 객체를 생성할 수 있게 합니다.
    public FileChannelRepository() {
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
        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}