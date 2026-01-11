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
@RequestMapping("/api/readStatus")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    /**
     * 읽음 상태 생성
     * POST /api/readStatus
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest request) {
        ReadStatus readStatus = readStatusService.create(request);
        return ResponseEntity.ok(readStatus);
    }

    /**
     * ID로 읽음 상태 조회
     * GET /api/readStatus/{id}
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<ReadStatus> findById(@PathVariable UUID id) {
        ReadStatus readStatus = readStatusService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("읽음 상태를 찾을 수 없습니다."));
        return ResponseEntity.ok(readStatus);
    }

    /**
     * 특정 유저의 모든 읽음 상태 조회
     * GET /api/readStatus/user/{userId}
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> findAllByUserId(@PathVariable UUID userId) {
        List<ReadStatus> readStatuses = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatuses);
    }

    /**
     * 읽음 상태 업데이트
     * PATCH /api/readStatus
     */
    @RequestMapping(method = RequestMethod.PATCH)
    public ResponseEntity<ReadStatus> update(@RequestBody ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusService.update(request);
        return ResponseEntity.ok(readStatus);
    }

    /**
     * 읽음 상태 삭제
     * DELETE /api/readStatus/{id}
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        readStatusService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


