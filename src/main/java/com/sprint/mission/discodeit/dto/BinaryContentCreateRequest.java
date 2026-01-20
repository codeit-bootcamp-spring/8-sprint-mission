package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BinaryContentCreateRequest {
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String bytes; // Base64 encoded file data
}