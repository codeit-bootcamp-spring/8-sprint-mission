package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BinaryContentCreateRequest(
    @NotBlank(message = "파일명은 필수입니다.")
    @Size(max = 255)
    String fileName,

    @NotBlank(message = "Content-Type은 필수입니다.")
    @Size(max = 100)
    String contentType,

    @NotNull(message = "파일 내용은 필수입니다.")
    byte[] bytes
) {

}
