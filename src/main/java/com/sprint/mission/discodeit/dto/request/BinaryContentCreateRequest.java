package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BinaryContentCreateRequest(

    @NotBlank(message = "파일명은 필수입니다.")
    String fileName,

    @NotBlank(message = "파일 타입은 필수입니다.")
    String contentType,

    //notnull은  " " 공백은 통과시킴 (null차단)
    //notempty는 null은 통과시킴 (크기 0차단)
    @NotNull(message = "파일 데이터는 null일 수 없습니다.")
    @NotEmpty(message = "파일 내용이 비어있습니다.")
    byte[] bytes
) {

}
