package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.request.ChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/channel")
public class ChannelController {
    private final ChannelService channelService;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public ResponseEntity<ChannelResponse> create(@RequestParam(value = "type") ChannelStatus status,
                                  @RequestBody ChannelCreateRequest channelCreateRequest) {
        if(status.equals(ChannelStatus.PRIVATE)) {
            return ResponseEntity.ok(channelService.createPrivate(channelCreateRequest));
        }
        else {
            return ResponseEntity.ok(channelService.createPublic(channelCreateRequest));
        }
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<List<ChannelResponse>> findById(@RequestParam(value="id") UUID id) {
        List<ChannelResponse> channelResponse = channelService.findAll(id);
        return ResponseEntity.ok(channelResponse);
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public ResponseEntity<ChannelResponse> update(@RequestParam(value = "id") UUID id,
                                                  @RequestBody ChannelUpdateRequest channelUpdateRequest) {
        ChannelResponse channelResponse = channelService.update(id, channelUpdateRequest);
        return ResponseEntity.ok(channelResponse);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        channelService.delete(id);
        return ResponseEntity.ok().build();
    }
}
