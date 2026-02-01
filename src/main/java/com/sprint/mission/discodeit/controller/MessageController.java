package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.PageResponse;
import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController implements MessageApi {

  private final MessageService messageService;

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<MessageDto> create(
      @RequestPart(value = "messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    List<BinaryContentCreateRequest> attachmentRequests = Optional.ofNullable(attachments)
        .map(files -> files.stream()
            .map(file -> {
              try {
                return new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
                );
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
            .toList())
        .orElse(new ArrayList<>());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(messageService.create(messageCreateRequest, attachmentRequests));
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @RequestParam(value = "channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor,
      @PageableDefault(
          size = 50,
          sort = "createdAt",
          direction = Sort.Direction.DESC
      ) Pageable pageable
  ) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messageService.findAllByChannelId(channelId, cursor, pageable));
  }

  @PatchMapping(path = "{messageId}")
  public ResponseEntity<MessageDto> update(
      @PathVariable(value = "messageId") UUID messageId,
      @RequestBody MessageUpdateRequest messageUpdateRequest) {
    System.out.println(messageUpdateRequest.newContent());
    MessageDto messageResponse = messageService.update(messageId, messageUpdateRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messageResponse);
  }

  @DeleteMapping(path = "{messageId}")
  public ResponseEntity<Void> delete(
      @PathVariable(value = "messageId") UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
