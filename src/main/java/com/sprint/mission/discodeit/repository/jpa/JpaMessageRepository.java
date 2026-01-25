package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaMessageRepository implements MessageRepository {
    private final JpaRepositoryInterface jpaRepository;

    public JpaMessageRepository(JpaRepositoryInterface jpaRepository) {
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
    @Transactional
    public void deleteByChannelId(UUID channelId) {
        jpaRepository.deleteByChannelId(channelId);
    }

    public static interface JpaRepositoryInterface extends JpaRepository<Message, UUID> {
        @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC")
        List<Message> findAllByChannelIdOrderByCreatedAtDesc(@Param("channelId") UUID channelId);
        
        @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC")
        Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(@Param("channelId") UUID channelId);
        
        @Modifying
        @Query("DELETE FROM Message m WHERE m.channel.id = :channelId")
        void deleteByChannelId(@Param("channelId") UUID channelId);
    }
}
