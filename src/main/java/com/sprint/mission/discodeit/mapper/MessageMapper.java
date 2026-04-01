package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class, UserMapper.class})
public interface MessageMapper {

		@Mapping(target = "channelId", source = "channel.id")
		@Mapping(target = "channel", source = "channel.id")
		@Mapping(target = "authorId", source = "author.id")
		@Mapping(target = "author", source = "author")
		@Mapping(target = "attachmentIds", expression = "java(toAttachmentIds(message.getAttachments()))")
		@Mapping(target = "attachments", source = "attachments")
		MessageDto toDto(Message message);

		default List<UUID> toAttachmentIds(List<BinaryContent> attachments) {
				if (attachments == null) {
						return List.of();
				}
				return attachments.stream().map(BinaryContent::getId).toList();
		}
}
