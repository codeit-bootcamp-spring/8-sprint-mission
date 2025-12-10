package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    // 채널 생성
    Channel createChannel(String name, String description);

    // 채널 조회 (단건)
    Channel findChannel(UUID id);

    // 채널 조회 (다건)
    List<Channel> findAllChannels();

    // 수정
    Channel updateChannel(UUID id, String name, String description);

    // 삭제
    void deleteChannel(UUID id);
}
