package com.sprint.mission.discodeit.dto.channel;


import java.util.List;
import java.util.UUID;

/*
    ChannelCreatePrivateRequest
    -------------------------
    PRIVATE 채널을 생성용 DTO

    [필드 설명]
    • memberUserIds: 이 PRIVATE 채널에 참여할 사용자들의 id 목록
    "PRIVATE 채널을 생성할 때 채널에 참여하는 User 별 ReadStatus 정보를 생성"
    이라고 했으므로, 어떤 유저들이 참여하는지 정보가 필요합니다.
 */
public record ChannelCreatePrivateRequest(
        List<UUID> memberUserIds
) {
}
