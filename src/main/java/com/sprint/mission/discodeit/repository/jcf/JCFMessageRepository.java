package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data;

    public JCFMessageRepository() {
        /*
        //기존에 사용했던 HashMap은 모든 HTTP 요청이 같은 HashMap에 접근하기 때문에 Tread-Safe 하지 못함
        //기본적으로 @Repository scope는 Singleton
        //Tread-Safe한 ConcurrentHashMap사용
        */
        this.data = new ConcurrentHashMap<>();
    }

    @Override
    public Message save(Message message) {
        this.data.put(message.getId(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    @Override
    public Optional<Instant> findLatestMessageTimeByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder());
    }

    @Override
    public List<Message> findAll() {
        return this.data.values().stream().toList();
    }

    @Override
    public void delete(UUID id) {
        this.data.remove(id);
    }

    @Override
    public void deleteAllByChannelId(UUID ChannelId) {
        data.values().removeIf(message -> message.getChannelId().equals(ChannelId));
    }
}
