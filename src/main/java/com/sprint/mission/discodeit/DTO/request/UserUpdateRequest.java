package com.sprint.mission.discodeit.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수정할 User 정보")
public record UserUpdateRequest(
    @NotBlank(message = "유저 이름은 필수 정보입니다.")
    @Size(min = 2, max = 50, message = "유저 이름의 길이는 2이상 50이하여야 합니다.")
    String newUsername,

    @NotBlank(message = "이메일은 필수 정보입니다.")
    @Email(message = "유효하지 않는 이메일 형식입니다.")
    String newEmail,

    @Size(min = 8, message = "패스워드는 8자 이상이여야 합니다.")
    String newPassword
) {

}
