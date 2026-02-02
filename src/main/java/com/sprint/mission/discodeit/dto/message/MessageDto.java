package com.sprint.mission.discodeit.dto.message;


import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/*
    MessageDto
    -------------------------
    메시지 조회용 DTO

    [필드 설명]
    • id                : 메시지 id
    • createdAt         : 메시지 작성 시간
    • updatedAt         : 메시지 수정 시간
    • content           : 메시지 내용
    • channelId         : 어느 채널인지
    • author            : 어떤 유저인지
    • attachments       : 첨부 파일(BinaryContent) 목록
 */
public record MessageDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    String content,
    UUID channelId,
    UserDto author,
    List<BinaryContentDto> attachments
) {

}
