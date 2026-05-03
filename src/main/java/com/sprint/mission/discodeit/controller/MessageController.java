package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/messages")
public class MessageController implements MessageApi {

  private final MessageService messageService;

  @Timed("message.create.async")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto> create(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    int fileCount = (attachments != null) ? attachments.size() : 0;
    log.info("[MessageController] 메시지 생성 요청 - 작성자 Id: {}, 파일: {}개", messageCreateRequest.authorId(),
        fileCount);

    List<BinaryContentCreateRequest> filesRequests = Optional.ofNullable(attachments)
        .map(files -> files.stream() // 요청들을 스트림화
            .map(BinaryContentCreateRequest::fileFromRequest)
            .toList() // 스트림을 List로 변환
        ).orElse(new ArrayList<>()); // 요청이 null일 경우 처리

    MessageDto message = messageService.create(messageCreateRequest, filesRequests);

    log.info("[MessageController] 메시지 생성 완료 - Id: {}", message.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(message);
  }

  @PatchMapping(value = "/{messageId}")
  public ResponseEntity<MessageDto> update(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest messageUpdateRequest
  ) {
    log.info("[MessageController] 메시지 수정 요청 - Id: {}", messageId);

    MessageDto message = messageService.update(messageId, messageUpdateRequest);

    log.info("[MessageController] 메시지 수정 완료 - Id: {}", message.id());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(message);
  }

  @DeleteMapping(value = "/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    log.info("[MessageController] 메시지 삭제 요청 - Id: {}", messageId);

    messageService.delete(messageId);

    log.info("[MessageController] 메시지 삭제 완료 - Id: {}", messageId);
    return ResponseEntity
        .status(HttpStatus.OK)
        .build();
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @RequestParam("channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor, // 'page' 대신 'cursor'
      @PageableDefault(
          size = 50,
          sort = "createdAt",
          direction = Direction.DESC
      ) Pageable pageable
  ) {
    PageResponse<MessageDto> messages = messageService.findAllByChannelId(channelId, cursor,
        pageable);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messages);
  }
}
