package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
  ADMIN("관리자"),
  CHANNEL_MANAGER("채널 매니저"),
  USER("일반 사용자");

  private final String description;
}
