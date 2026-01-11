package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final BinaryContentService binaryContentService;

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
    @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<Message> create(
            @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        
        List<UUID> attachmentIds = new ArrayList<>();
        
        // 첨부 파일이 있는 경우 BinaryContent로 저장
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    try {
                        BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest(
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getSize(),
                                Base64.getEncoder().encodeToString(file.getBytes())
                        );
                        attachmentIds.add(binaryContentService.create(binaryRequest).getId());
                    } catch (Exception e) {
                        throw new RuntimeException("파일 첨부 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        // MessageCreateRequest에 attachmentIds 설정
        messageCreateRequest = new MessageCreateRequest(
                messageCreateRequest.getAuthorId(),
                messageCreateRequest.getChannelId(),
                messageCreateRequest.getContent(),
                attachmentIds
        );
        
        // 메시지 생성
        Message message = messageService.create(messageCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
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