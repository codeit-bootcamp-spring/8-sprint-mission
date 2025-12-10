package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    //메시지 생성
    void createMessage(Message message);

    Message create(UUID uid, UUID cid, String chnnalName, String from, String content);

    //단건 조회
    Message findById(UUID id);

    //다건 조회
    //- 전체 조회
    List<Message> findMessages();
    //- 발신자별 조회
    Map<String, List<Message>> findMessagesByFrom();

    //메시지 수정
    void updateMessage(UUID id, String fieldName, String value);

    //메시지 삭제
    void deleteMessage(UUID id);

    //메시지 조회
    Optional<Message> findByMId(UUID id);

    //메시지 저장
    void saveMessage(Message message);
}
