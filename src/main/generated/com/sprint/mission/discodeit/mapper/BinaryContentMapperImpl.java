package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-01T00:46:57+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.16 (Oracle Corporation)"
)
@Component
public class BinaryContentMapperImpl implements BinaryContentMapper {

    @Override
    public BinaryContentDto toDto(BinaryContent binaryContent) {
        if ( binaryContent == null ) {
            return null;
        }

        UUID id = null;
        String fileName = null;
        Long size = null;
        String contentType = null;

        id = binaryContent.getId();
        fileName = binaryContent.getFileName();
        size = binaryContent.getSize();
        contentType = binaryContent.getContentType();

        BinaryContentDto binaryContentDto = new BinaryContentDto( id, fileName, size, contentType );

        return binaryContentDto;
    }
}
