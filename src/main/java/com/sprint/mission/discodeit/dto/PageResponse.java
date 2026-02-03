package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private Integer number;
    private Integer size;
    private Boolean hasNext;
    private Long totalElements;
}
