package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.MessageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

@Tag(name = "Message", description = "Message API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  @Operation(summary = "Message 생성", operationId = "create_2")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "404",
          description = "Channel 또는 User를 찾을 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "Channel | Author with id {channelId | authorId} not found")
          )
      ),
      @ApiResponse(
          responseCode = "201",
          description = "Channel 또는 User를 찾을 수 없음",
          content = @Content(
              schema = @Schema(
                  implementation = MessageResponse.class
              )
          )
      )
  })
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<MessageResponse> create(
      @Parameter(name = "messageCreateRequest", description = "Message 생성 요청 정보", required = true)
      @RequestPart(value = "messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @Parameter(name = "attachments", description = "Message 첨부 파일들")
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

  @Operation(summary = "Channel의 Message 목록 조회", operationId = "findAllByChannelId")
  @ApiResponse(
      responseCode = "200",
      description = "Message 목록 조회 성공",
      content = @Content(
          schema = @Schema(implementation = MessageResponse.class)
      )
  )
  @GetMapping
  public ResponseEntity<List<MessageResponse>> findAllByChannelId(
      @Parameter(description = "조회할 Channel ID", required = true)
      @RequestParam(value = "channelId") UUID channelId) {
    List<MessageResponse> messageResponses = messageService.findAllByChannelId(channelId);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messageResponses);
  }

  @Operation(summary = "Message 내용 수정", operationId = "update_2")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Message가 성공적으로 수정됨",
          content = @Content(
              schema = @Schema(implementation = MessageResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Message를 찾을 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "Message with id {messageId} not found")
          )
      )
  })
  @PatchMapping(path = "{messageId}")
  public ResponseEntity<MessageResponse> update(
      @Parameter(description = "수정할 Message ID", required = true)
      @PathVariable(value = "messageId") UUID messageId,
      @RequestBody MessageUpdateRequest messageUpdateRequest) {
    System.out.println(messageUpdateRequest.newContent());
    MessageResponse messageResponse = messageService.update(messageId, messageUpdateRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messageResponse);
  }

  @Operation(summary = "Message 삭제", operationId = "delete_1")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204",
          description = "Message가 성공적으로 삭제됨"
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Message를 찾을 수 없음",
          content = @Content(
              examples = @ExampleObject(value = "Message with id {messageId} not found")
          )
      )
  })
  @DeleteMapping(path = "{messageId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Message ID", required = true)
      @PathVariable(value = "messageId") UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
