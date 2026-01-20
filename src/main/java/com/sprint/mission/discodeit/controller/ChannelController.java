package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    /**
     * User가 참여 중인 Channel 목록 조회
     * GET /api/channels?userId=...
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelResponse> findAllByUserId(@RequestParam UUID userId) {
        return channelService.findAllByUserId(userId);
    }

    /**
     * Public Channel 생성
     * POST /api/channels/public
     */
    @RequestMapping(value = "/public", method = RequestMethod.POST, consumes = {"application/json", "multipart/form-data"})
    public ChannelResponse createPublic(@RequestPart(value = "channelCreateRequest", required = false) ChannelCreateRequest requestPart,
                                       @RequestBody(required = false) ChannelCreateRequest requestBody) {
        // multipart/form-data로 오는 경우와 JSON으로 오는 경우 모두 처리
        ChannelCreateRequest request = requestPart != null ? requestPart : requestBody;
        if (request == null) {
            throw new IllegalArgumentException("ChannelCreateRequest가 필요합니다.");
        }
        return channelService.createPublic(request);
    }

    /**
     * Private Channel 생성
     * POST /api/channels/private
     */
    @RequestMapping(value = "/private", method = RequestMethod.POST, consumes = {"application/json", "multipart/form-data"})
    public ChannelResponse createPrivate(@RequestPart(value = "channelCreateRequest", required = false) ChannelCreateRequest requestPart,
                                        @RequestBody(required = false) ChannelCreateRequest requestBody) {
        // multipart/form-data로 오는 경우와 JSON으로 오는 경우 모두 처리
        ChannelCreateRequest request = requestPart != null ? requestPart : requestBody;
        if (request == null) {
            throw new IllegalArgumentException("ChannelCreateRequest가 필요합니다.");
        }
        return channelService.createPrivate(request);
    }

    /**
     * Channel 정보 수정
     * PATCH /api/channels/{channelId}
     */
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ChannelResponse update(@PathVariable UUID channelId, @RequestBody ChannelUpdateRequest request) {
        // 경로 파라미터의 channelId를 사용하여 ChannelUpdateRequest 생성
        ChannelUpdateRequest updateRequest = new ChannelUpdateRequest(channelId, request.getName(), request.getDescription());
        return channelService.update(updateRequest);
    }

    /**
     * Channel 삭제
     * DELETE /api/channels/{channelId}
     */
    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }
}