package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

    /*
    NotNull : null인지만 확인 " " 공백은 통과
    NotEmpty : 길이가 0인지만 확인 " " 공백은 통과
    NotBlank : null도 안되고 길이가 0인것도 안되고 " " 공백도 안됨(가장 강력)
     */

    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(min = 2, max = 15, message = "이름은 2자 이상 15자 사이여야 합니다.")
    String username,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 50, message = "이메일은 50자 이내여야 합니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    String password
) {

}
