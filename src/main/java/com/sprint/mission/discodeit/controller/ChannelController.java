package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    // 공개 채널 생성 (GET-only 미션 대응)
    @RequestMapping("create-public")
    public ResponseEntity<ChannelResponse> createPublic(
            @ModelAttribute ChannelCreatePublicRequest request)
    {
        ChannelResponse response = channelService.createPublicChannel(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 비공개 채널 생성 (GET-only 미션 대응)
    @RequestMapping("create-private")
    public ResponseEntity<ChannelResponse> createPrivate(
            @ModelAttribute ChannelCreatePrivateRequest request)
    {
        ChannelResponse response = channelService.createPrivateChannel(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 공개 채널 수정 (GET-only 미션 대응)
    @RequestMapping("update")
    public ResponseEntity<ChannelResponse> updateChannel(@ModelAttribute ChannelUpdateRequest request) {
        ChannelResponse response = channelService.updateChannel(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 채널 삭제 (GET-only 미션 대응)
    @RequestMapping("delete")
    public ResponseEntity<Void> deleteChannel(@RequestParam("channelId") UUID channelId) {
        channelService.deleteChannel(channelId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    // 특정 사용자가 볼 수 있는 모든 채널 목록 조회
    @RequestMapping("findAllByUser")
    public ResponseEntity<List<ChannelResponse>> findAllByUserId(@RequestParam("userId") UUID userId) {
        List<ChannelResponse> response = channelService.findAllByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
