package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Channel", description = "Channel API")
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  // 공개 채널 생성 (GET-only 미션 대응)
  @Operation(summary = "Public Channel 생성")
  @ApiResponse(responseCode = "201", description = "Public Channel이 성공적으로 생성됨")
  @PostMapping("public")
  public ResponseEntity<ChannelResponse> create(
      @RequestBody ChannelCreatePublicRequest request) {
    ChannelResponse response = channelService.createPublicChannel(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  // 비공개 채널 생성 (GET-only 미션 대응)
  @Operation(summary = "Private Channel 생성")
  @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨")
  @PostMapping("private")
  public ResponseEntity<ChannelResponse> create(
      @RequestBody ChannelCreatePrivateRequest request) {
    ChannelResponse response = channelService.createPrivateChannel(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  // 공개 채널 수정 (GET-only 미션 대응)
  @Operation(summary = "Channel 정보 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "Channel with id {channelId} not found"
              )
          )),
      @ApiResponse(responseCode = "400", description = "Private Channel은 수정할 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "Private channel cannot be updated"
              )
          )),
      @ApiResponse(responseCode = "200", description = "Channel 정보가 성공적으로 수정됨")
  })
  @PatchMapping("{channelId}")
  public ResponseEntity<ChannelResponse> update(
      @Parameter(
          description = "수정할 Channel ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID channelId,
      @RequestBody ChannelUpdateRequest request) {
    ChannelResponse response = channelService.updateChannel(channelId, request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

  // 채널 삭제 (GET-only 미션 대응)
  @Operation(summary = "Channel 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음",
          content = @Content(
              mediaType = "*/*",
              examples = @ExampleObject(
                  value = "Channel with id {channelId} not found"
              )
          )),
      @ApiResponse(responseCode = "204",
          description = "Channel이 성공적으로 삭제됨")
  })
  @DeleteMapping("{channelId}")
  public ResponseEntity<Void> delete(
      @Parameter(
          description = "삭제할 Channel ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @PathVariable UUID channelId) {
    channelService.deleteChannel(channelId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  // 특정 사용자가 볼 수 있는 모든 채널 목록 조회
  @Operation(summary = "User가 참여 중인 Channel 목록 조회")
  @ApiResponse(responseCode = "200", description = "Channel 목록 조회 성공")
  @GetMapping
  public ResponseEntity<List<ChannelResponse>> findAll(
      @Parameter(
          description = "조회할 User ID",
          schema = @Schema(type = "string", format = "uuid")
      )
      @RequestParam("userId") UUID userId) {
    List<ChannelResponse> response = channelService.findAllByUserId(userId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

}
