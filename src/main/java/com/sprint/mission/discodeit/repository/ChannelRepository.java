package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository {

    // 채널 생성
    Channel save(Channel channel);

    // 채널 조회 (단건)
    Channel findById(UUID id);

    // 채널 조회 (다건)
    List<Channel> findAll();

    // 채널 수정
    Channel update(UUID id, Channel updateChannel);

    // 채널 삭제
    void delete(UUID id);
}
