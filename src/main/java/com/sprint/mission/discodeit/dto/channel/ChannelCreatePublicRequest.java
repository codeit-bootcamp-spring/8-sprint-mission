package com.sprint.mission.discodeit.dto.channel;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
    ChannelCreatePublicRequest
    -------------------------
    PUBLIC 채널을 생성용 DTO

    [필드 설명]
    • name              : 채널 이름
    • description       : 채널 설명
 */
public record ChannelCreatePublicRequest(

    @NotBlank
    @Size(max = 100)
    String name,

    @NotBlank
    @Size(max = 500)
    String description
) {

}
