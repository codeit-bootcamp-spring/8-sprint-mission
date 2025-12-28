package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import java.util.UUID;

@Getter
// @NoArgsConstructor // ✅ 에러의 원인: 아래 직접 만든 생성자와 중복되므로 삭제합니다.
public class BinaryContent {
    private UUID id;

    // 직접 작성한 기본 생성자: 객체가 생성될 때 고유 ID를 부여합니다.
    public BinaryContent() {
        this.id = UUID.randomUUID(); //
    }
}