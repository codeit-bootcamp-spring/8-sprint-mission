package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface MessageService {

  MessageDto create(MessageCreateRequest request,
      List<BinaryContentCreateRequest> binaryContentCreateRequests);

  //단건 조회
  MessageDto findById(UUID messageId);

  //다건 조회
  //- 전체 조회
  PageResponse<MessageDto> findAllByChannelId(UUID messageId, Instant cursor,
      Pageable pageable);

  //메시지 수정
  MessageDto update(UUID id, MessageUpdateRequest request);

  //메시지 삭제
  void delete(UUID messageId);
}
