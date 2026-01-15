package com.sprint.mission.discodeit.api;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Message", description = "Message API")
public interface MessageApi {

  @Operation(summary = "Message 생성")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음",
          content = @Content(mediaType = "*/*",
              examples = @ExampleObject(value = "Channel | Author with id {channelId | authorId} not found"))),
      @ApiResponse(responseCode = "201", description = "Message가 성공적으로 생성됨")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<MessageDto> create(
      @RequestPart MessageCreateRequest messageCreateRequest,
      @Parameter(description = "Message 첨부 파일들")
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments);

  @Operation(summary = "Message 내용 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음",
          content = @Content(mediaType = "*/*",
              examples = @ExampleObject(value = "Message with id {messageId} not found")))
  })
  @PatchMapping("/{messageId}")
  ResponseEntity<MessageDto> update(
      @Parameter(description = "수정할 Message ID", schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request);

  @Operation(summary = "Message 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Message가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음",
          content = @Content(mediaType = "*/*",
              examples = @ExampleObject(value = "Message with id {messageId} not found")))
  })
  @DeleteMapping("/{messageId}")
  ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Message ID", schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID messageId);

  @Operation(summary = "Channel의 Message 목록 조회")
  @ApiResponse(responseCode = "200", description = "Message 목록 조회 성공")
  @GetMapping
  ResponseEntity<List<MessageDto>> findAllByChannelId(
      @Parameter(description = "조회할 Channel ID", schema = @Schema(type = "string", format = "uuid"))
      @RequestParam("channelId") UUID channelId);
}
