package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-01T00:46:56+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.16 (Oracle Corporation)"
)
@Component
public class ChannelMapperImpl implements ChannelMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ChannelDto toDto(Channel channel, List<User> participants, Instant lastMessageAt) {
        if ( channel == null && participants == null && lastMessageAt == null ) {
            return null;
        }

        UUID id = null;
        ChannelType type = null;
        String name = null;
        String description = null;
        if ( channel != null ) {
            id = channel.getId();
            type = channel.getType();
            name = channel.getName();
            description = channel.getDescription();
        }
        List<UserDto> participants1 = null;
        participants1 = userListToUserDtoList( participants );
        Instant lastMessageAt1 = null;
        lastMessageAt1 = lastMessageAt;

        ChannelDto channelDto = new ChannelDto( id, type, name, description, participants1, lastMessageAt1 );

        return channelDto;
    }

    protected List<UserDto> userListToUserDtoList(List<User> list) {
        if ( list == null ) {
            return null;
        }

        List<UserDto> list1 = new ArrayList<UserDto>( list.size() );
        for ( User user : list ) {
            list1.add( userMapper.toDto( user ) );
        }

        return list1;
    }
}
