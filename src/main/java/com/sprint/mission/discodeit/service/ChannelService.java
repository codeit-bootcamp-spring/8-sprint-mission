package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelService {

    // 생성 책임: 이름과 소유자 ID를 받아 새로운 채널 객체를 생성 후 저장
    Channel create(String name, UUID ownerId);

    // 수정 책임: 채널 ID와 변경할 이름 및 소유자 ID를 받아 채널 정보를 수정
    Channel update(UUID channelId, String newName, UUID newOwnerId);

    Optional<Channel> findById(UUID id);
    List<Channel> findAll();
    void delete(UUID id);
}