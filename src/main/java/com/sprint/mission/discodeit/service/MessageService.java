package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    // 메시지 등록
    Message createMessage(UUID userId, UUID channelId, String contents);

    // 메시지 조회 (단건)
    Message findMessage(UUID id);

    // 메시지 조회 (다건)
    List<Message> findAllMessages();
    
    // 수정
    Message updateMessage(UUID id, String contents);
    
    // 삭제
    void deleteMessage(UUID id);
}
