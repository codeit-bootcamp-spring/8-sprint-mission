package com.sprint.mission.discodeit.dto.message;


import jakarta.validation.constraints.NotBlank;

/*
    MessageUpdateRequest
    -------------------------
    메시지 수정용 DTO

    [필드 설명]
    • newContent          : 메시지 내용
 */
public record MessageUpdateRequest(
    
    @NotBlank
    String newContent
) {

}
