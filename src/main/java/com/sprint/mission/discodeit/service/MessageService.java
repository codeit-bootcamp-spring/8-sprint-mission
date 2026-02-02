package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;


/*
    MessageService
    -------------------------
    메시지 관련 비즈니스 로직을 담당하는 서비스
 */
public interface MessageService {

  // 메시지 등록
  MessageDto createMessage(MessageCreateRequest request);

  // 메시지 조회 (단건)
  MessageDto findMessage(UUID id);

  PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable);

  // 수정
  MessageDto updateMessage(UUID messageId, MessageUpdateRequest request);

  // 삭제
  void deleteMessage(UUID id);
}
