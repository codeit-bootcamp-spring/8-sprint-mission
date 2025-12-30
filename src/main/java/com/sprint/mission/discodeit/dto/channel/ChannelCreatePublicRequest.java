package com.sprint.mission.discodeit.dto.channel;


/*
    ChannelCreatePublicRequest
    -------------------------
    PUBLIC 채널을 생성용 DTO

    [필드 설명]
    • name              : 채널 이름
    • description       : 채널 설명
 */
public record ChannelCreatePublicRequest(
    String name,
    String description
) {

}
