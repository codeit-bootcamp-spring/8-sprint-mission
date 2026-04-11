package com.sprint.mission.discodeit.auth.dto;

public record CsrfTokenResponse(
    String token,
    String headerName,
    String parameterName
) {

}

