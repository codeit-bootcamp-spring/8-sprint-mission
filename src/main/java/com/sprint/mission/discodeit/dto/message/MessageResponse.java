package com.sprint.mission.discodeit.dto.message;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

/*
    MessageDto
    -------------------------
    메시지 조회용 DTO

    [필드 설명]
    • id                : 메시지 id
    • channelId         : 어느 채널인지
    • authorId          : 어떤 유저가 작성 했는지
    • content           : 메시지 내용
    • createdAt         : 메시지 작성 시간
    • attachmentIds     : 첨부 파일(BinaryContent) id 목록
 */
public record MessageResponse(
    UUID id,
    UUID channelId,
    UUID authorId,
    String content,
    Instant createdAt,
    List<UUID> attachmentIds
) {

}
