package com.sprint.mission.discodeit.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Public Channel 생성 정보")
public record PublicChannelCreateRequest(
    @NotBlank(message = "채널 이름은 필수 정보입니다.")
    @Size(min = 2, max = 100, message = "채널 이름의 길이는 2이상 100이하 입니다.")
    String name,

    @Size(max = 500, message = "채널 설명은 500자까지 허용합니다.")
    String description
) {

}
