package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    /**
     * Public 채널 생성
     * POST /channels/public
     */
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelResponse createPublic(@RequestBody ChannelCreateRequest request) {
        return channelService.createPublic(request);
    }

    /**
     * Private 채널 생성
     * POST /channels/private
     */
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelResponse createPrivate(@RequestBody ChannelCreateRequest request) {
        return channelService.createPrivate(request);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelResponse> findAll() {
        return channelService.findAll();
    }

    /**
     * 특정 사용자가 볼 수 있는 모든 채널 목록 조회
     * GET /channels/user/{userId}
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public List<ChannelResponse> findAllByUserId(@PathVariable UUID userId) {
        return channelService.findAllByUserId(userId);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Optional<ChannelResponse> findById(@PathVariable UUID id) {
        return channelService.findById(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ChannelResponse update(@PathVariable UUID id, @RequestParam String name, @RequestParam String description) {
        return channelService.update(id, name, description);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        channelService.delete(id);
    }
}