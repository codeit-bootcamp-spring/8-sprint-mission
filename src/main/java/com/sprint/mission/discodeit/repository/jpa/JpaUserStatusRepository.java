package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserStatusRepositoryInterface extends JpaRepository<UserStatus, UUID> {
    Optional<UserStatus> findByUserId(UUID userId);
}

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaUserStatusRepository implements UserStatusRepository {
    private final JpaUserStatusRepositoryInterface jpaRepository;

    public JpaUserStatusRepository(JpaUserStatusRepositoryInterface jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserStatus save(UserStatus userStatus) {
        return jpaRepository.save(userStatus);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public java.util.List<UserStatus> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }
}
