package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/channels")
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  @PostMapping("public")
  public ResponseEntity<ChannelDto> createPublicChannel(
      @Valid @RequestBody PublicChannelCreateRequest channelCreateRequest) {
    log.info("Controller: Public 채널 생성 요청 - name: {}", channelCreateRequest.name());
    ChannelDto channelDto = channelService.create(channelCreateRequest);
    log.info("Controller: Public 채널 생성 완료 - ID: {}", channelDto.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelDto);
  }

  @PostMapping("private")
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @Valid @RequestBody PrivateChannelCreateRequest channelCreateRequest) {
    log.info("Controller: Private 채널 생성 요청 - participants: {}", channelCreateRequest.participantIds());
    ChannelDto channelDto = channelService.create(channelCreateRequest);
    log.info("Controller: Private 채널 생성 완료 - ID: {}", channelDto.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(channelDto);
  }

  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAll(
      @RequestParam(value = "userId") UUID userId) {
    List<ChannelDto> channelResponse = channelService.findAll(userId);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channelResponse);
  }

  @PatchMapping("{channelId}")
  public ResponseEntity<ChannelDto> update(
      @PathVariable("channelId") UUID channelId,
      @Valid @RequestBody PublicChannelUpdateRequest channelUpdateRequest) {
    log.info("Controller: Public 채널 수정 요청 - ID: {}", channelId);
    ChannelDto channelResponse = channelService.update(channelId, channelUpdateRequest);
    log.info("Controller: Public 채널 수정 완료 - ID: {}", channelId);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channelResponse);
  }

  @DeleteMapping(value = "{channelId}")
  public ResponseEntity<Void> delete(
      @PathVariable("channelId") UUID channelId) {
    log.info("Controller: 채널 삭제 요청 - ID: {}", channelId);
    channelService.delete(channelId);
    log.info("Controller: 채널 삭제 완료 - ID: {}", channelId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
