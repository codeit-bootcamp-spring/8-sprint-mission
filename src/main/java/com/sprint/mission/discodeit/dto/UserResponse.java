package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter //  필수: 이게 없으면 JSON 결과가 { } 로 비어 보입니다.
@AllArgsConstructor
public class UserResponse {
    private UUID id;            // 1. 유저 ID
    private String name;        // 2. 이름
    private String email;       // 3. 이메일
    private UUID statusId;      // 4. 상태 객체 ID
    private UUID userId;        // 5. 유저 참조 ID
    private boolean isOnline;   // 6. 온라인 여부 (멘토 피드백 반영)
    private UUID profileImage;// 7. 프로필 이미지 (멘토 피드백 반영)
}