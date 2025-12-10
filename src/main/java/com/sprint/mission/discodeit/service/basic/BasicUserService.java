package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BasicUserService implements UserService {
    private final UserRepository userRepository;

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(User user) {

    }

    @Override
    public User create(String userId, String name, String email, char gender, int grade) {
        User user = new User(userId, name, email, gender, grade);
        userRepository.save(user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return null;
    }


    @Override
    public List<User> findUsers() {
        return List.of();
    }

    @Override
    public Map<Integer, List<User>> findUserByGrade() {
        return Map.of();
    }

    @Override
    public void updateUser(UUID id, String fieldName, Object value) {

    }

    @Override
    public void deleteUser(UUID id) {

    }

    @Override
    public Optional<User> findByUId(UUID id) {
        return Optional.empty();
    }

    @Override
    public void saveUser(User user) {

    }
}
