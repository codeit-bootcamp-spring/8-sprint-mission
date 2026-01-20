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
     * GET /api/messages?channelId=...&userId=...
     * userId는 선택적: 개인 채널의 경우 권한 체크를 위해 필요하지만, PUBLIC 채널은 선택적
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<MessageResponse> findByChannelId(
            @RequestParam UUID channelId, 
            @RequestParam(required = false) UUID userId) {
        // 입력 검증
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID가 필요합니다.");
        }
        
        // userId가 제공된 경우 권한 체크 포함 조회
        if (userId != null && messageService instanceof com.sprint.mission.discodeit.service.basic.BasicMessageService) {
            List<MessageResponse> responses = ((com.sprint.mission.discodeit.service.basic.BasicMessageService) messageService)
                    .findByChannelId(channelId, userId);
            
            // 최종 검증: 모든 응답의 channelId가 요청한 channelId와 일치하는지 확인
            for (MessageResponse response : responses) {
                if (!response.getChannelId().equals(channelId)) {
                    throw new IllegalStateException("응답에 다른 채널의 메시지가 포함되어 있습니다.");
                }
            }
            
            return responses;
        }
        
        // userId가 없는 경우 기본 조회 (PUBLIC 채널용, 권한 체크 없음)
        List<MessageResponse> responses = messageService.findByChannelId(channelId);
        
        // 채널 ID 검증
        for (MessageResponse response : responses) {
            if (!response.getChannelId().equals(channelId)) {
                throw new IllegalStateException("응답에 다른 채널의 메시지가 포함되어 있습니다.");
            }
        }
        
        return responses;
    }

    /**
     * Message 생성 (multipart/form-data)
     * POST /api/messages
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<Message> createMultipart(
            @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        
        List<UUID> attachmentIds = new ArrayList<>();
        
        // 첨부 파일이 있는 경우 BinaryContent로 저장하고 attachmentIds에 추가
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    try {
                        byte[] fileBytes = file.getBytes();
                        String base64Bytes = Base64.getEncoder().encodeToString(fileBytes);
                        
                        BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest(
                                file.getOriginalFilename(),
                                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                                file.getSize(),
                                base64Bytes
                        );
                        attachmentIds.add(binaryContentService.create(binaryRequest).getId());
                    } catch (Exception e) {
                        throw new RuntimeException("파일 첨부 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        // 기존 attachmentIds가 있으면 추가
        if (messageCreateRequest.getAttachmentIds() != null) {
            attachmentIds.addAll(messageCreateRequest.getAttachmentIds());
        }
        
        // MessageCreateRequest에 attachmentIds 설정
        MessageCreateRequest finalRequest = new MessageCreateRequest(
                messageCreateRequest.getAuthorId(),
                messageCreateRequest.getChannelId(),
                messageCreateRequest.getContent(),
                attachmentIds
        );
        
        // 메시지 생성
        Message message = messageService.create(finalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
    
    /**
     * Message 생성 (application/json)
     * POST /api/messages
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Message> createJson(@RequestBody MessageCreateRequest messageCreateRequest) {
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