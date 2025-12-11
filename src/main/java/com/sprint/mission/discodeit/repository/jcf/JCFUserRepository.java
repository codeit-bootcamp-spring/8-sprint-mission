package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.*;
import java.util.stream.Collectors;

public class JCFUserRepository implements UserRepository {

    private static JCFUserRepository INSTANCE;
    private final Map<UUID, User> data;

    private JCFUserRepository() {
        this.data = new HashMap<>();
    }

    public static JCFUserRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JCFUserRepository();
        }
        return INSTANCE;
    }

    @Override
    public User save(User user) {
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<User> findAll() {
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}