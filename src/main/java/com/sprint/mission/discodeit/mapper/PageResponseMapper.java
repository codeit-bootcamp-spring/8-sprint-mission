package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.response.PageResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

@Mapper(componentModel = "spring")
public interface PageResponseMapper {

  default <T> PageResponse<T> fromSlice(
      Slice<T> slice,
      java.util.function.Function<T, Object> cursorExtractor
  ) {
    Object nextCursor = null;

    if (!slice.isEmpty()) {
      T lastElement = slice.getContent()
          .get(slice.getNumberOfElements() - 1);
      nextCursor = cursorExtractor.apply(lastElement);
    }

    return new PageResponse<>(
        slice.getContent(),
        nextCursor,
        slice.getSize(),
        slice.hasNext(),
        null
    );
  }

  default <T> PageResponse<T> fromPage(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        null,
        page.getSize(),
        page.hasNext(),
        page.getTotalElements()
    );
  }
}
