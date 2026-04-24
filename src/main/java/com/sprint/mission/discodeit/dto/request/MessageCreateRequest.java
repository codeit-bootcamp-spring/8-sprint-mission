package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MessageCreateRequest(
    @NotNull(message = "메시지 데이터 형식이 잘못되었습니다.")
    @Size(max = 2000, message = "메시지 내용은 2000자 이하여야 합니다")
    String content,

    @NotNull(message = "채널 ID는 필수입니다")
    UUID channelId
) {

}
