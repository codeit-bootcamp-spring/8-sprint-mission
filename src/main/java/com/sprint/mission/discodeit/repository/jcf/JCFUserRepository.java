package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {
    @Override
    public User save(User user) {
        return user;
    }

    @Override
    public Optional<User> findByUserId(UUID id) {
        return Optional.empty();
    }


}
