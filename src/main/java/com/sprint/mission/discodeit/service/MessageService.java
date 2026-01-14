package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.response.MessageResponse;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  MessageResponse create(MessageCreateRequest request,
      List<BinaryContentCreateRequest> binaryContentCreateRequests);

  //단건 조회
  MessageResponse findById(UUID id);

  //다건 조회
  //- 전체 조회
  List<MessageResponse> findAllByChannelId(UUID id);

  //메시지 수정
  MessageResponse update(UUID id, MessageUpdateRequest request);

  //메시지 삭제
  void delete(UUID id);
}
