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
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public MessageResponse create(@RequestParam String content, @RequestParam UUID authorId, @RequestParam UUID channelId) {
        return messageService.create(content, authorId, channelId);
    }

    @RequestMapping(value = "/channel/{channelId}", method = RequestMethod.GET)
    public List<MessageResponse> findByChannelId(@PathVariable UUID channelId) {
        return messageService.findByChannelId(channelId);
    }

    /**
     * 메시지 수정
     * PATCH /messages
     */
    @RequestMapping(method = RequestMethod.PATCH)
    public ResponseEntity<Message> update(@RequestBody MessageUpdateRequest request) {
        Message updatedMessage = messageService.update(request);
        return ResponseEntity.ok(updatedMessage);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        messageService.delete(id);
    }
}