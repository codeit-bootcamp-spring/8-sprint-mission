package com.sprint.mission.discodeit.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Message 생성 정보")
public record MessageCreateRequest(
    @NotNull(message = "사용자 아이디가 필요합니다.")
    UUID authorId,

    @NotNull(message = "채널 아이디가 필요합니다.")
    UUID channelId,

    @Size(max = 2000, message = "메시지 내용은 2000자까지 허용합니다.")
    String content
) {

}
