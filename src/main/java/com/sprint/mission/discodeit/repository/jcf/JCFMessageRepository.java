package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> database = new HashMap<>();

    @Override
    public Message save(Message message) {
        database.put(message.getId(), message);
        return message;
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    // [멘토님 피드백 반영] 특정 채널 ID에 해당하는 메시지만 효율적으로 삭제
    @Override
    public void deleteByChannelId(UUID channelId) {
        // 모든 데이터를 서비스로 넘기지 않고 리포지토리 내부에서 처리합니다.
        database.values().removeIf(message -> message.getChannelId().equals(channelId));
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return List.of();
    }

    @Override
    public Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId) {
        return Optional.empty();
    }
}