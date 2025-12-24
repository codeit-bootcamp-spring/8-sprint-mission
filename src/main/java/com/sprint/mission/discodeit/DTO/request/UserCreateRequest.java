package com.sprint.mission.discodeit.DTO.request;

public record UserCreateRequest(
        String userId,
        String name,
        String password,
        String email,
        char gender,
        int grade
) {
}
