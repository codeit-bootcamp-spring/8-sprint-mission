package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepositoryInterface extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaUserRepository implements UserRepository {
    private final JpaUserRepositoryInterface jpaRepository;

    public JpaUserRepository(JpaUserRepositoryInterface jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public java.util.List<User> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
