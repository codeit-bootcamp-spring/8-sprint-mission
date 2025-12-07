package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.*;
import java.util.stream.Collectors;

public class JCFChannelService implements ChannelService {

    // 싱글톤 패턴 구현
    private static JCFChannelService INSTANCE;

    // JCF 필드
    private final Map<UUID, Channel> data;

    // private 생성자
    private JCFChannelService() {
        this.data = new HashMap<>();
    }

    // 싱글톤 인스턴스 반환 메서드
    public static JCFChannelService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JCFChannelService();
        }
        return INSTANCE;
    }

    // --- CRUD 구현 ---

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
    public Channel update(Channel channel) {
        if (data.containsKey(channel.getId())) {
            data.put(channel.getId(), channel);
            return channel;
        }
        throw new NoSuchElementException("수정할 Channel ID가 존재하지 않습니다: " + channel.getId());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}