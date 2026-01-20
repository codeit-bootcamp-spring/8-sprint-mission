package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusService {

    // DTO를 활용해 파라미터를 그룹화하여 생성합니다.
    ReadStatus create(ReadStatusCreateRequest request);

    // ID로 특정 읽음 상태를 조회합니다.
    Optional<ReadStatus> findById(UUID id);

    // 특정 유저(userId)가 가진 모든 채널의 읽음 상태 목록을 조회합니다.
    List<ReadStatus> findAllByUserId(UUID userId);

    // DTO를 활용해 마지막으로 읽은 메시지 ID를 업데이트합니다.
    ReadStatus update(ReadStatusUpdateRequest request);

    // ID로 읽음 상태 객체를 삭제합니다.
    void delete(UUID id);
}