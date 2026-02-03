package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class Pageable {
    private Integer page;
    private Integer size;
    private List<String> sort;
}
