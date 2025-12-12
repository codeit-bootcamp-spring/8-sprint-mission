package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User create(String name, String gender, Integer age) {
        User user = new User(name, gender, age);

        return userRepository.save(user);
    }

    @Override
    public User findUser(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findAllGender(String gender) {
        return userRepository.findAllGender(gender);
    }

    @Override
    public List<User> findAllAge() {
        return userRepository.findAllAge();
    }

    @Override
    public User update(UUID id, User updateUser) {
        // 기존 유저 조회
        User user = userRepository.findById(id);

        if(user == null) throw new IllegalArgumentException("해당 유저가 존재하지 않습니다.");

        user.update(updateUser);

        return userRepository.update(id, user);
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }
}
