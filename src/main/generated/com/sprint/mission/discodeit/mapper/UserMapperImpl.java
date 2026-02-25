package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
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
public class UserMapperImpl implements UserMapper {

    private final BinaryContentMapper binaryContentMapper;

    @Autowired
    public UserMapperImpl(BinaryContentMapper binaryContentMapper) {

        this.binaryContentMapper = binaryContentMapper;
    }

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String username = null;
        String email = null;
        BinaryContentDto profile = null;

        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        profile = binaryContentMapper.toDto( user.getProfile() );

        boolean online = user.getStatus() != null && user.getStatus().isOnline();

        UserDto userDto = new UserDto( id, username, email, profile, online );

        return userDto;
    }
}
