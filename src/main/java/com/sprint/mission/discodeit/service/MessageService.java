package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    // 생성 책임: 발신자 ID, 채널 ID, 내용을 받아 메시지 객체를 생성 후 저장
    Message create(UUID senderId, UUID channelId, String content);

    // 수정 책임: 메시지 ID와 변경할 내용(content)을 받아 메시지를 수정
    Message update(UUID messageId, String newContent);

    Optional<Message> findById(UUID id);
    List<Message> findAll();
    void delete(UUID id);
}