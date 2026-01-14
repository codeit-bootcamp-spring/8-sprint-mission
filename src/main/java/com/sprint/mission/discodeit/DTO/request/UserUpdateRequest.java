package com.sprint.mission.discodeit.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import java.util.UUID;

@Schema(description = "수정할 User 정보")
public record UserUpdateRequest(
    String newUsername,
    String newEmail,
    String newPassword

) {

}
