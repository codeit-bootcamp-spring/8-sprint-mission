package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicChannelUpdateRequest(

    @NotBlank(message = "변경할 채널 이름은 필수입니다.")
    @Size(min = 2, max = 15, message = "채널 이름은 2자 이상 15자 사이여야 합니다.")
    String newName,

    @Size(max = 100, message = "채널 설명은 100자 이내로 입력해주세요.")
    String newDescription
) {

}
