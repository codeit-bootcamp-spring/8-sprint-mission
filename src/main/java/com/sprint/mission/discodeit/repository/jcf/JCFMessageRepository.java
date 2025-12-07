package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.*;
import java.util.stream.Collectors;

public class JCFMessageRepository implements MessageRepository {

    private static JCFMessageRepository INSTANCE;
    private final Map<UUID, Message> data;

    private JCFMessageRepository() {
        this.data = new HashMap<>();
    }

    public static JCFMessageRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JCFMessageRepository();
        }
        return INSTANCE;
    }

    @Override
    public Message save(Message message) {
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Message> findAll() {
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}