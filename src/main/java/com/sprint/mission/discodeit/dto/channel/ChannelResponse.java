package com.sprint.mission.discodeit.dto.channel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/*
    ChannelDto
    -------------------------
    채널 조회용 DTO

    [필드 설명]
    • id                 : 채널 id
    • name               : 채널 이름
    • description        : 채널 설명
    • type               : PUBLIC / PRIVATE 구분
    • lastMessageAt      : 가장 최근 메시지 시각 (없으면 null)
    • participantIds     : PRIVATE 채널인 경우 참여한 유저 id 목록
 */
public record ChannelResponse(
    UUID id,
    String name,
    String description,
    String type,
    Instant lastMessageAt,
    List<UUID> participantIds
) {

}
