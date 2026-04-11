package com.sprint.mission.discodeit.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthSuccessResponse<T>(
    boolean success,
    T data,
    String message
) {

  public AuthSuccessResponse(T data) {
    this(true, data, null);
  }

  public AuthSuccessResponse(T data, String message) {
    this(true, data, message);
  }
}

