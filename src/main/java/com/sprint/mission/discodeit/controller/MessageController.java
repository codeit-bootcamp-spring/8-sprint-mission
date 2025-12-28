package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 메시지 생성 (GET-only 미션 대응)
    @RequestMapping("create")
    public ResponseEntity<MessageResponse> createMessage(@ModelAttribute MessageCreateRequest request,
                                                         @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {

        List<BinaryContentCreateRequest> attachmentList = toBinaryRequests(attachments);

        MessageCreateRequest msgRequest = new MessageCreateRequest(
                request.channelId(),
                request.userId(),
                request.contents(),
                attachmentList
        );

        MessageResponse created = messageService.createMessage(msgRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // 메시지 수정 (GET-only 미션 대응)
    @RequestMapping("update")
    public ResponseEntity<MessageResponse> update(@ModelAttribute MessageUpdateRequest request) {
        MessageResponse message = messageService.updateMessage(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(message);
    }

    // 메시지 삭제 (GET-only 미션 대응)
    @RequestMapping("delete")
    public ResponseEntity<Void> delete(@RequestParam("messageId") UUID messageId) {
        messageService.deleteMessage(messageId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    // 특정 채널의 메시지 목록을 조회 (GET-only 미션 대응)
    @RequestMapping("findAllByChannel")
    public ResponseEntity<List<MessageResponse>> findAllByChannel(@RequestParam("channelId") UUID channelId) {
        List<MessageResponse> messageList = messageService.findAllByChannelId(channelId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(messageList);
    }

    private List<BinaryContentCreateRequest> toBinaryRequests(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of(); // 불변의 비어있는 리스트 return
        }

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(this::toBinaryRequest)
                .toList();
    }


    private BinaryContentCreateRequest toBinaryRequest(MultipartFile file) {
        try {
            return new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "첨부파일을 읽을 수 없습니다. " + file.getOriginalFilename(), e
            );
        }
    }


}
