package com.sprint.mission.discodeit.dto.message;


import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import java.util.List;
import java.util.UUID;

/*
    MessageCreateRequest
    -------------------------
    메시지 생성용 DTO

    [필드 설명]
    • channelId         : 메시지가 속할 채널 id
    • authorId          : 메시지를 보낸 사용자 id
    • content           : 메시지 내용
    • attachments       : 첨부파일 목록 (선택)
 */
public record MessageCreateRequest(
    UUID channelId,
    UUID authorId,
    String content,
    List<BinaryContentCreateRequest> attachments
) {

}
