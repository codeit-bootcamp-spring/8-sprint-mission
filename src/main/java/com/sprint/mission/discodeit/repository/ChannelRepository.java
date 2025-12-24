package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {

    // 채널 생성
    Channel save(Channel channel);

    // 채널 조회 (단건)
    Optional<Channel> findById(UUID id);

    // 채널 조회 (다건)
    List<Channel> findAll();

    // 존재 여부
    boolean existsById(UUID id);

    // 채널 삭제
    void delete(UUID id);
}
