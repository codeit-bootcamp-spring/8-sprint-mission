package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {

    // 메시지 등록
    Message save(Message message);

    // 메시지 조회 (단건)
    Optional<Message> findById(UUID id);

    // 메시지 조회 (다건)
    List<Message> findAll();

    // 존재 여부
    boolean existsById(UUID id);

    // 삭제
    void delete(UUID id);

    // 특정 채널에 속한 모든 메시지 목록 반환
    List<Message> findAllByChannelId(UUID channelId);
}
