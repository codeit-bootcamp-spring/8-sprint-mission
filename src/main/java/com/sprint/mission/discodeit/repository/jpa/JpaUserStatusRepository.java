package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaUserStatusRepository implements UserStatusRepository {
    private final JpaRepositoryInterface jpaRepository;

    public JpaUserStatusRepository(JpaRepositoryInterface jpaRepository) {
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

    public static interface JpaRepositoryInterface extends JpaRepository<UserStatus, UUID> {
        @Query("SELECT us FROM UserStatus us WHERE us.user.id = :userId")
        Optional<UserStatus> findByUserId(@Param("userId") UUID userId);
    }
}
