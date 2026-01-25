package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReadStatusRepositoryInterface extends JpaRepository<ReadStatus, UUID> {
    List<ReadStatus> findAllByUserId(UUID userId);
    List<ReadStatus> findAllByChannelId(UUID channelId);
    
    @Query("SELECT rs FROM ReadStatus rs WHERE rs.userId = :userId AND rs.channelId = :channelId")
    Optional<ReadStatus> findByUserIdAndChannelId(@Param("userId") UUID userId, @Param("channelId") UUID channelId);
}

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaReadStatusRepository implements ReadStatusRepository {
    private final JpaReadStatusRepositoryInterface jpaRepository;

    public JpaReadStatusRepository(JpaReadStatusRepositoryInterface jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        return jpaRepository.save(readStatus);
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ReadStatus> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId);
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return jpaRepository.findAllByChannelId(channelId);
    }
}
