package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class JCFUserRepository implements UserRepository {

    private final Map<UUID, User> data = new HashMap<>();

    @Override
    public User save(User user) {
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public List<User> findAllGender(String gender) {
        return data.values().stream()
                .filter(user -> Objects.equals(user.getGender(), gender))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllAge() {
        return data.values().stream()
                .sorted(Comparator.comparing(User::getAge))
                .collect(Collectors.toList());
    }

    @Override
    public User update(UUID id, User updateUser) {
        data.put(id,  updateUser);
        return updateUser;
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
