package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;

    public BasicMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void createMessage(Message message) {
    }

    @Override
    public Message create(UUID uid, UUID cid, String chnnalName, String from, String content) {
        Message message = new Message(uid, cid, chnnalName, from, content);
        messageRepository.save(message);
        return message;
    }


    @Override
    public Message findById(UUID id) {
        return null;
    }

    @Override
    public List<Message> findMessages() {
        return List.of();
    }

    @Override
    public Map<String, List<Message>> findMessagesByFrom() {
        return Map.of();
    }

    @Override
    public void updateMessage(UUID id, String fieldName, String value) {

    }

    @Override
    public void deleteMessage(UUID id) {

    }

    @Override
    public Optional<Message> findByMId(UUID id) {
        return Optional.empty();
    }

    @Override
    public void saveMessage(Message message) {

    }
}
