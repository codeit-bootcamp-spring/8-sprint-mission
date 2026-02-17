package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 1, max = 50, message = "사용자명은 1~50자여야 합니다.")
    String newUsername,

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100)
    String newEmail,

    @Size(max = 60, message = "비밀번호는 60자 이하여야 합니다.")
    String newPassword
) {

}
