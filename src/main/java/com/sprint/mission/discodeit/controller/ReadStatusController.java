package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/readStatus")
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    @RequestMapping(value ="/create", method = RequestMethod.GET)
    public ResponseEntity<ReadStatusResponse> create(@RequestBody ReadStatusCreateRequest readStatusCreateRequest) {
        return ResponseEntity.ok(readStatusService.create(readStatusCreateRequest));
    }

    @RequestMapping(value = "/find/{userId}", method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatusResponse>> find(@PathVariable UUID userId) {
        List<ReadStatusResponse> readStatusResponses = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatusResponses);
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public ResponseEntity<ReadStatusResponse> update(@RequestParam(value = "userId") UUID userId,
                                                     @RequestParam(value = "channelId") UUID channelId,
                                                     @RequestBody ReadStatusUpdateRequest readStatusUpdateRequest) {
        ReadStatusResponse readStatusResponse = readStatusService.update(userId, channelId, readStatusUpdateRequest);
        return ResponseEntity.ok(readStatusResponse);
    }
}
