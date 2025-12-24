package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {
    Channel save(Channel channel);
    Optional<Channel> findById(UUID id);
    List<Channel> findAll();
    List<Channel> findAllByStatus(ChannelStatus status);
    void delete(UUID id);
    List<Channel> findAllPrivateChannelIdsByUserId(UUID uId);
}
