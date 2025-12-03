package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {

    // 메시지 등록
    Message save(Message message);

    // 메시지 조회 (단건)
    Message findById(UUID id);

    // 메시지 조회 (다건)
    List<Message> findAll();

    // 수정
    Message update(UUID id, Message updateMessage);

    // 삭제
    void delete(UUID id);
}
