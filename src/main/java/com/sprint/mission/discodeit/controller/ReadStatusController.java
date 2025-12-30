package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  /*
      특정 채널의 메시지 수신 정보 생성
   */
  @Operation(description = "Message 읽음 상태 생성")
  @ApiResponses({
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "Channel | User with id {channelId | userId} not found"
              )
          )),
      @ApiResponse(responseCode = "400", description = "이미 읽음 상태가 존재함",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "ReadStatus with userId {userId} and channelId {channelId} already exists"
              )
          )),
      @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨")
  })
  @PostMapping
  public ResponseEntity<ReadStatusResponse> create(
      @RequestBody ReadStatusCreateRequest request) {
    ReadStatusResponse created = readStatusService.create(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(created);
  }

  /*
      특정 채널의 메시지 수신 정보 수정
      - 서비스에서 해당 ReadStatus를 찾아 lastReadAt을 갱신한다.
   */
  @Operation(summary = "Message 읽음 상태 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Message 읽음 상태가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message 읽음 상태를 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "ReadStatus with id {readStatusId} not found"
              )
          )
      )
  })
  @PatchMapping("{readStatusId}")
  public ResponseEntity<ReadStatusResponse> update(
      @Parameter(
          description = "수정할 읽음 상태 ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request) {
    ReadStatusResponse updated = readStatusService.update(readStatusId, request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updated);
  }

  /*
      특정 사용자의 메시지 수신 정보 조회
      - userId로 해당 유저의 ReadStatus 목록을 전부 조회한다.
   */
  @Operation(summary = "User의 Message 읽음 상태 목록 조회")
  @ApiResponse(responseCode = "200", description = "Message 읽음 상태 목록 조회 성공")
  @GetMapping
  public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(
      @Parameter(
          description = "조회할 User ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @RequestParam("userId") UUID userId) {
    List<ReadStatusResponse> list = readStatusService.findAllByUserId(userId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(list);
  }

}
