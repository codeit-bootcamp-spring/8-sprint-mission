package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/*
    ChannelDto
    -------------------------
    채널 조회용 DTO

    [필드 설명]
    • id                 : 채널 id
    • type               : PUBLIC / PRIVATE 구분
    • name               : 채널 이름
    • description        : 채널 설명
    • participants       : PRIVATE 채널인 경우 참여한 유저 목록
    • lastMessageAt      : 가장 최근 메시지 시각 (없으면 null)
 */
public record ChannelDto(
    UUID id,
    ChannelType type,
    String name,
    String description,
    List<UserDto> participants,
    Instant lastMessageAt
) {

}
