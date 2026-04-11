package com.sprint.mission.discodeit.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthErrorResponse(
    boolean success,
    String error,
    String message
) {

  public AuthErrorResponse(String error, String message) {
    this(false, error, message);
  }
}

