package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController implements ReadStatusApi {

  private final ReadStatusService readStatusService;

  /*
      특정 채널의 메시지 수신 정보 생성
   */
  @Override
  public ResponseEntity<ReadStatusResponse> create(ReadStatusCreateRequest request) {
    ReadStatusResponse created = readStatusService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /*
      특정 채널의 메시지 수신 정보 수정
      - 서비스에서 해당 ReadStatus를 찾아 lastReadAt을 갱신한다.
   */
  @Override
  public ResponseEntity<ReadStatusResponse> update(UUID readStatusId,
      ReadStatusUpdateRequest request) {
    ReadStatusResponse updated = readStatusService.update(readStatusId, request);
    return ResponseEntity.ok(updated);
  }

  /*
      특정 사용자의 메시지 수신 정보 조회
      - userId로 해당 유저의 ReadStatus 목록을 전부 조회한다.
   */
  @Override
  public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(UUID userId) {
    List<ReadStatusResponse> list = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(list);
  }

}
