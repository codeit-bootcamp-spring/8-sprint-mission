package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
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
@RequestMapping("/api/readStatus")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    /*
        특정 채널의 메시지 수신 정보 생성
     */
    @RequestMapping("create")
    public ResponseEntity<ReadStatusResponse> create(@ModelAttribute ReadStatusCreateRequest request) {
        ReadStatusResponse created = readStatusService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    /*
        특정 채널의 메시지 수신 정보 수정
        - ReadStatusUpdateRequest: (id, lastReadAt)
        - 서비스에서 해당 ReadStatus를 찾아 lastReadAt을 갱신한다.
     */
    @RequestMapping("update")
    public ResponseEntity<ReadStatusResponse> update(@ModelAttribute ReadStatusUpdateRequest request) {
        ReadStatusResponse updated = readStatusService.update(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updated);
    }

    /*
        특정 사용자의 메시지 수신 정보 조회
        - userId로 해당 유저의 ReadStatus 목록을 전부 조회한다.
     */
    @RequestMapping("findAllByUser")
    public ResponseEntity<List<ReadStatusResponse>> findAllByUser(@RequestParam("userId") UUID userId) {
        List<ReadStatusResponse> list = readStatusService.findAllByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(list);
    }

}
