package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    // 고도화: DTO 활용
    Message create(MessageCreateRequest request);

    Optional<Message> findById(UUID id);

    // 고도화: 특정 Channel의 Message 목록을 조회하도록 변경
    List<Message> findAllByChannelId(UUID channelId);

    // 고도화: DTO 활용
    Message update(MessageUpdateRequest request);

    void delete(UUID id);

    MessageResponse create(String content, UUID authorId, UUID channelId);

    List<MessageResponse> findByChannelId(UUID channelId);
}