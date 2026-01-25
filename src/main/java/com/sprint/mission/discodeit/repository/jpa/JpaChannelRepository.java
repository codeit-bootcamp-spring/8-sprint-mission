package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface JpaChannelRepositoryInterface extends JpaRepository<Channel, UUID> {
}

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaChannelRepository implements ChannelRepository {
    private final JpaChannelRepositoryInterface jpaRepository;

    public JpaChannelRepository(JpaChannelRepositoryInterface jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Channel save(Channel channel) {
        return jpaRepository.save(channel);
    }

    @Override
    public java.util.Optional<Channel> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public java.util.List<Channel> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
