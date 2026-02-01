package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.DTO.dto.BinaryContentDto;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
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
public class UserMapperImpl implements UserMapper {

    @Autowired
    private BinaryContentMapper binaryContentMapper;

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        Boolean online = null;
        UUID id = null;
        String username = null;
        String email = null;
        BinaryContentDto profile = null;

        online = mapOnlineStatus( user.getUserStatus() );
        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        profile = binaryContentMapper.toDto( user.getProfile() );

        UserDto userDto = new UserDto( id, username, email, profile, online );

        return userDto;
    }
}
