package com.sprint.mission.discodeit.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "수정할 Message 내용")
public record MessageUpdateRequest(
    @Size(max = 2000, message = "메시지 내용은 2000자까지만 하용합니다.")
    String newContent
) {

}
