// BasicUserService는 특별한 비즈니스 로직이 없으므로, Repository에 위임만 합니다.
package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    // 생성자를 통해 Repository 인터페이스를 주입받음
    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user); // 저장 로직은 Repository에 위임
    }
    // ... 나머지 findById, findAll, update, delete는 모두 userRepository에 위임
    @Override
    public Optional<User> findById(UUID id) { return userRepository.findById(id); }
    @Override
    public List<User> findAll() { return userRepository.findAll(); }
    @Override
    public User update(User user) { return userRepository.save(user); }
    @Override
    public void delete(UUID id) { userRepository.delete(id); }
}