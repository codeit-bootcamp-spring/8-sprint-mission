package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Channel의 Message 목록 조회
     * GET /api/messages?channelId=...
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<MessageResponse> findByChannelId(@RequestParam UUID channelId) {
        return messageService.findByChannelId(channelId);
    }

    /**
     * Message 생성
     * POST /api/messages
     */
    @RequestMapping(method = RequestMethod.POST)
    public MessageResponse create(@RequestParam String content, @RequestParam UUID authorId, @RequestParam UUID channelId) {
        return messageService.create(content, authorId, channelId);
    }

    /**
     * Message 내용 수정
     * PATCH /api/messages/{messageId}
     */
    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<Message> update(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest request) {
        // 경로 파라미터의 messageId를 사용하여 MessageUpdateRequest 생성
        MessageUpdateRequest updateRequest = new MessageUpdateRequest(messageId, request.getContent(), request.getAttachmentIds());
        Message updatedMessage = messageService.update(updateRequest);
        return ResponseEntity.ok(updatedMessage);
    }

    /**
     * Message 삭제
     * DELETE /api/messages/{messageId}
     */
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }
}