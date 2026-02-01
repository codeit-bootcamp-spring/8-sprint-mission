package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-01T00:46:57+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.16 (Oracle Corporation)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BinaryContentMapper binaryContentMapper;

    @Override
    public MessageDto toDto(Message message) {
        if ( message == null ) {
            return null;
        }

        UUID channelId = null;
        UUID id = null;
        Instant createdAt = null;
        Instant updatedAt = null;
        String content = null;
        UserDto author = null;
        List<BinaryContentDto> attachments = null;

        channelId = messageChannelId( message );
        id = message.getId();
        createdAt = message.getCreatedAt();
        updatedAt = message.getUpdatedAt();
        content = message.getContent();
        author = userMapper.toDto( message.getAuthor() );
        attachments = binaryContentListToBinaryContentDtoList( message.getAttachments() );

        MessageDto messageDto = new MessageDto( id, createdAt, updatedAt, content, channelId, author, attachments );

        return messageDto;
    }

    private UUID messageChannelId(Message message) {
        if ( message == null ) {
            return null;
        }
        Channel channel = message.getChannel();
        if ( channel == null ) {
            return null;
        }
        UUID id = channel.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<BinaryContentDto> binaryContentListToBinaryContentDtoList(List<BinaryContent> list) {
        if ( list == null ) {
            return null;
        }

        List<BinaryContentDto> list1 = new ArrayList<BinaryContentDto>( list.size() );
        for ( BinaryContent binaryContent : list ) {
            list1.add( binaryContentMapper.toDto( binaryContent ) );
        }

        return list1;
    }
}
