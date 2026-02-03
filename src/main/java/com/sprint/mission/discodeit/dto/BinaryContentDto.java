package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class BinaryContentDto {

  private UUID id;
  private String fileName;
  private Long size;
  private String contentType;
  private byte[] bytes;

  public BinaryContentDto(UUID id, String fileName, Long size, String contentType) {
  }
}
