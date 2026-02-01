package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Message", description = "Message API")
public interface MessageApi {

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
                  implementation = MessageDto.class
              )
          )
      )
  })
  ResponseEntity<MessageDto> create(
      @Parameter(name = "messageCreateRequest", description = "Message 생성 요청 정보", required = true) MessageCreateRequest messageCreateRequest,
      @Parameter(name = "attachments", description = "Message 첨부 파일들") List<MultipartFile> attachments
  );

  @Operation(summary = "Channel의 Message 목록 조회", operationId = "findAllByChannelId")
  @ApiResponse(
      responseCode = "200",
      description = "Message 목록 조회 성공",
      content = @Content(
          schema = @Schema(implementation = MessageDto.class)
      )
  )
  ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @Parameter(description = "조회할 Channel ID", required = true) UUID channelId,
      @Parameter(description = "페이징 커서 정보") Instant cursor,
      @Parameter(description = "페이징 정보", required = true) Pageable pageable

  );

  @Operation(summary = "Message 내용 수정", operationId = "update_2")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Message가 성공적으로 수정됨",
          content = @Content(
              schema = @Schema(implementation = MessageDto.class)
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
  ResponseEntity<MessageDto> update(
      @Parameter(description = "수정할 Message ID", required = true) UUID messageId,
      @Parameter(description = "수정할 Message 내용") MessageUpdateRequest request
  );

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
  ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Message ID", required = true) UUID messageId
  );
}
