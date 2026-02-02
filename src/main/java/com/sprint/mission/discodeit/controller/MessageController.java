package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.MessageApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController implements MessageApi {

  private final MessageService messageService;

  // 메시지 생성 (GET-only 미션 대응)
  @Override
  public ResponseEntity<MessageDto> create(
      MessageCreateRequest messageCreateRequest,
      List<MultipartFile> attachments) {

    List<BinaryContentCreateRequest> attachmentRequests = toBinaryRequests(attachments);

    // 서비스 레이어에 맞는 최종 요청 객체 생성
    MessageCreateRequest msgRequest = new MessageCreateRequest(
        messageCreateRequest.channelId(),
        messageCreateRequest.authorId(),
        messageCreateRequest.content(),
        attachmentRequests
    );

    MessageDto created = messageService.createMessage(msgRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  // 메시지 수정
  @Override
  public ResponseEntity<MessageDto> update(UUID messageId, MessageUpdateRequest request) {
    return ResponseEntity.ok(messageService.updateMessage(messageId, request));
  }

  // 메시지 삭제
  @Override
  public ResponseEntity<Void> delete(UUID messageId) {
    messageService.deleteMessage(messageId);
    return ResponseEntity.noContent().build();
  }

  // 특정 채널의 메시지 목록 조회 (50개, 최신순, Slice 기반)
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(UUID channelId,
      Pageable pageable) {
    return ResponseEntity.ok(messageService.findAllByChannelId(channelId, pageable));
  }

  private List<BinaryContentCreateRequest> toBinaryRequests(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return List.of(); // 불변의 비어있는 리스트 return
    }

    return files.stream()
        .filter(file -> file != null && !file.isEmpty())
        .map(this::toBinaryRequest)
        .toList();
  }


  private BinaryContentCreateRequest toBinaryRequest(MultipartFile file) {
    try {
      return new BinaryContentCreateRequest(
          file.getOriginalFilename(),
          file.getSize(),
          file.getContentType(),
          file.getBytes()
      );
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "첨부파일을 읽을 수 없습니다. " + file.getOriginalFilename(), e
      );
    }
  }


}
