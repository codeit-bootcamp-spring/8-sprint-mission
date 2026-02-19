package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
    ChannelUpdateRequest
    -------------------------
    채널 정보를 수정할 때 사용하는 DTO

    [필드 설명]
    • newName              : 변경할 채널 이름
    • newDescription       : 변경할 설명
 */
public record ChannelUpdateRequest(

    @NotBlank
    @Size(max = 100)
    String newName,

    @NotBlank
    @Size(max = 500)
    String newDescription
) {

}
