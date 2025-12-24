package com.sprint.mission.discodeit.dto.channel;

import java.util.UUID;

/*
    ChannelUpdateRequest
    -------------------------
    채널 정보를 수정할 때 사용하는 DTO

    [필드 설명]
    • id                : 수정 대상 채널 id
    • name              : 변경할 채널 이름
    • description       : 변경할 설명
 */
public record ChannelUpdateRequest(
        UUID id,
        String name,
        String description
) {
}
