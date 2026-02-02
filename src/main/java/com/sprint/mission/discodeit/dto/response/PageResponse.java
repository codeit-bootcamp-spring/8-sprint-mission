package com.sprint.mission.discodeit.dto.response;

import java.util.List;

/*
    PageResponse<T>
    -------------------------
    페이지네이션 응답 DTO

    [필드 설명]
    • content           : 실제 데이터
    • number            : 페이지 번호
    • size              : 페이지의 크기
    • totalElements     : T 데이터의 총 갯수를 의미하며, null일 수 있습니다.
 */
public record PageResponse<T>(
    List<T> content,
    int number,
    int size,
    boolean hasNext,
    Long totalElements
) {

}
