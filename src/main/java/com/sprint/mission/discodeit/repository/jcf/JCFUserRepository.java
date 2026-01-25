package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFUserRepository implements UserRepository {
    private final List<User> list = new ArrayList<>();

    @Override
    public User save(User user) {
        list.removeIf(e -> e.getId().equals(user.getId()));
        list.add(user);
        return user;

    }

    @Override
    public Optional<User> findById(UUID id) {
        return list.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return list.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }
    @Override
    public List<User> findAll() {
        return new ArrayList<>(list);
    }

    @Override
    public void delete(UUID id) {
        list.removeIf(e -> e.getId().equals(id));
    }

    @Override
    public boolean existsByName(String name) {
        return list.stream().anyMatch(u -> u.getUsername().equals(name));
    }

    @Override
    public boolean existsByEmail(String email) {
        return list.stream().anyMatch(u -> u.getEmail().equals(email));
    }
}