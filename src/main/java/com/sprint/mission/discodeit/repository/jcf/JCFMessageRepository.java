package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.Optional;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    @Override
    public Message save(Message message) {
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.empty();
    }
}
