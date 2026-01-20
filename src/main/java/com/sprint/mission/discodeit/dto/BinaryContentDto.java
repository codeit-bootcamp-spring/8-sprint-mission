package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class BinaryContentDto {
    private UUID id;
    private String fileName;
    private Long size;
    private String contentType;
}
