package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
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
    date = "2026-02-24T13:37:41+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.16 (Oracle Corporation)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    private final UserMapper userMapper;
    private final BinaryContentMapper binaryContentMapper;

    @Autowired
    public MessageMapperImpl(UserMapper userMapper, BinaryContentMapper binaryContentMapper) {

        this.userMapper = userMapper;
        this.binaryContentMapper = binaryContentMapper;
    }

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
        Channel channel = message.getChannel();
        if ( channel == null ) {
            return null;
        }
        return channel.getId();
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
