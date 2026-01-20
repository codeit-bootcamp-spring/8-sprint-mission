package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    // DTO 기반 생성
    Message create(MessageCreateRequest request);

    // DTO 기반 수정
    Message update(MessageUpdateRequest request);

    Optional<Message> findById(UUID id);
    List<Message> findAll();
    List<Message> findAllByChannelId(UUID channelId);
    
    // Response DTO 반환 메서드
    List<MessageResponse> findByChannelId(UUID channelId);
    
    void delete(UUID id);
}