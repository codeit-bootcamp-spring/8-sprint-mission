package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name = "Message", description = "Message API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  // 메시지 생성 (GET-only 미션 대응)
  @Operation(summary = "Message 생성")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "Channel | Author with id {channelId | authorId} not found"
              )
          )),
      @ApiResponse(responseCode = "201",
          description = "Message가 성공적으로 생성됨")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageResponse> create(
      @RequestPart MessageCreateRequest messageCreateRequest,
      @Parameter(description = "Message 첨부 파일들")
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {

    List<BinaryContentCreateRequest> attachmentList = toBinaryRequests(attachments);

    MessageCreateRequest msgRequest = new MessageCreateRequest(
        messageCreateRequest.channelId(),
        messageCreateRequest.authorId(),
        messageCreateRequest.content(),
        attachmentList
    );

    MessageResponse created = messageService.createMessage(msgRequest);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(created);
  }

  // 메시지 수정 (GET-only 미션 대응)
  @Operation(summary = "Message 내용 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "Message with id {messageId} not found"
              )
          ))
  })
  @PatchMapping("{messageId}")
  public ResponseEntity<MessageResponse> update(
      @Parameter(
          description = "수정할 Message ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request) {
    MessageResponse message = messageService.updateMessage(messageId, request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(message);
  }

  // 메시지 삭제 (GET-only 미션 대응)
  @Operation(summary = "Message 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Message가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "Message with id {messageId} not found"
              )
          ))
  })
  @DeleteMapping("{messageId}")
  public ResponseEntity<Void> delete(
      @Parameter(
          description = "삭제할 Message ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID messageId) {
    messageService.deleteMessage(messageId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  // 특정 채널의 메시지 목록을 조회 (GET-only 미션 대응)
  @Operation(summary = "Channel의 Message 목록 조회")
  @ApiResponse(responseCode = "200", description = "Message 목록 조회 성공")
  @GetMapping
  public ResponseEntity<List<MessageResponse>> findAllByChannelId(
      @Parameter(
          description = "조회할 Channel ID",
          schema = @Schema(type = "string", format = "uuid")

      )
      @RequestParam("channelId") UUID channelId) {
    List<MessageResponse> messageList = messageService.findAllByChannelId(channelId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messageList);
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
