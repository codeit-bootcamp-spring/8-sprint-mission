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

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaReadStatusRepository implements ReadStatusRepository {
    private final JpaRepositoryInterface jpaRepository;

    public JpaReadStatusRepository(JpaRepositoryInterface jpaRepository) {
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

    public static interface JpaRepositoryInterface extends JpaRepository<ReadStatus, UUID> {
        @Query("SELECT rs FROM ReadStatus rs WHERE rs.user.id = :userId")
        List<ReadStatus> findAllByUserId(@Param("userId") UUID userId);
        
        @Query("SELECT rs FROM ReadStatus rs WHERE rs.channel.id = :channelId")
        List<ReadStatus> findAllByChannelId(@Param("channelId") UUID channelId);
        
        @Query("SELECT rs FROM ReadStatus rs WHERE rs.user.id = :userId AND rs.channel.id = :channelId")
        Optional<ReadStatus> findByUserIdAndChannelId(@Param("userId") UUID userId, @Param("channelId") UUID channelId);
    }
}
