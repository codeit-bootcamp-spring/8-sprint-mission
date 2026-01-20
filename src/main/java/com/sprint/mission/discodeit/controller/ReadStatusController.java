package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    /**
     * User의 Message 읽음 상태 목록 조회
     * GET /api/readStatuses?userId=...
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam UUID userId) {
        List<ReadStatus> readStatuses = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatuses);
    }

    /**
     * Message 읽음 상태 생성
     * POST /api/readStatuses
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest request) {
        ReadStatus readStatus = readStatusService.create(request);
        return ResponseEntity.ok(readStatus);
    }

    /**
     * Message 읽음 상태 수정
     * PATCH /api/readStatuses/{readStatusId}
     */
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatus> update(@PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request) {
        // 경로 파라미터의 readStatusId를 사용하여 ReadStatusUpdateRequest 생성
        ReadStatusUpdateRequest updateRequest = new ReadStatusUpdateRequest(readStatusId, request.getLastReadMessageId());
        ReadStatus readStatus = readStatusService.update(updateRequest);
        return ResponseEntity.ok(readStatus);
    }
}


