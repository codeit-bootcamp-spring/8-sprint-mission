package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BinaryContentStatus {
  PROCESSING("업로드 중"),
  SUCCESS("업로드 완료"),
  FAIL("업로드 실패");

  private final String description;
}
