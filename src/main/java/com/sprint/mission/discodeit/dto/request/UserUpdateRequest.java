package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

    @NotBlank(message = "변경할 사용자 이름은 필수입니다.")
    @Size(min = 2, max = 15, message = "이름은 2자 이상 15자 사이여야 합니다.")
    String newUsername,

    @NotBlank(message = "변경할 이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 50, message = "이메일은 50자 이내여야 합니다.")
    String newEmail,

    @NotBlank(message = "변경할 비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    String newPassword
) {

}
