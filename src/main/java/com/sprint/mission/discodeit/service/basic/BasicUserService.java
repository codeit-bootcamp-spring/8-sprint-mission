package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor; // 추가
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor // final 필드인 userRepository를 인자로 받는 생성자를 생성합니다.
public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    @Override
    public User create(String name, String email) {
        User user = new User(name, email);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User update(UUID id, String name, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.update(name, email);
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }
}