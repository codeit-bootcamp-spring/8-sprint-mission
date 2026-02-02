package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final BinaryContentMapper binaryContentMapper;
    private final UserMapper userMapper;

    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        // 작성자 변환
        UserDto authorDto = null;
        if (message.getAuthor() != null) {
            authorDto = userMapper.toDto(message.getAuthor());
        }

        // 첨부파일 변환
        List<BinaryContentDto> attachmentDtos = null;
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            attachmentDtos = message.getAttachments().stream()
                    .map(binaryContentMapper::toDto)
                    .collect(Collectors.toList());
        }

        return MessageDto.builder()
                .id(message.getId())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .content(message.getContent())
                .channelId(message.getChannelId())
                .author(authorDto)
                .attachments(attachmentDtos)
                .build();
    }
}
