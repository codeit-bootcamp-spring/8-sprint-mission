package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaMessageRepositoryInterface extends JpaRepository<Message, UUID> {
    List<Message> findAllByChannelIdOrderByCreatedAtDesc(UUID channelId);
    
    @Query("SELECT m FROM Message m WHERE m.channelId = :channelId ORDER BY m.createdAt DESC")
    Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(@Param("channelId") UUID channelId);
    
    void deleteByChannelId(UUID channelId);
}

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaMessageRepository implements MessageRepository {
    private final JpaMessageRepositoryInterface jpaRepository;

    public JpaMessageRepository(JpaMessageRepositoryInterface jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Message save(Message message) {
        return jpaRepository.save(message);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return jpaRepository.findAllByChannelIdOrderByCreatedAtDesc(channelId);
    }

    @Override
    public Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId) {
        return jpaRepository.findTopByChannelIdOrderByCreatedAtDesc(channelId);
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        jpaRepository.deleteByChannelId(channelId);
    }
}
