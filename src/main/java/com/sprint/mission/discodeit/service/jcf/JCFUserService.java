package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

public class JCFUserService implements UserService {
    private final List<User> data;
    private final UserRepository userRepository;

    //생성자 주입
    public JCFUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.data = new ArrayList<>();
    }

    @Override
    public void createUser(User user) {
        user.setModifiedAt(user.getCreatedAt());
        data.add(user);
    }

    @Override
    public User create(String userId, String name, String email, char gender, int grade) {
        User user =  new User(userId, name, email, gender, grade);
        user.setModifiedAt(user.getModifiedAt());
        data.add(user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return data.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findUsers() {
        return data;
    }

    @Override
    public Map<Integer, List<User>> findUserByGrade() {
        return data.stream()
                .collect(
                        Collectors.groupingBy(
                                User::getGrade,
                                Collectors.toList()
                ));
    }

    @Override
    public void updateUser(UUID id, String fieldName, Object value) {
        data.stream()
            .filter(user -> user.getId().equals(id))
            .forEach(user -> {
                if(fieldName.equals("userId")) {
                    if(value instanceof String) user.setUserId((String) value);
                    user.setModifiedAt(System.currentTimeMillis());
                } else if(fieldName.equals("name")) {
                    if(value instanceof String) user.setName((String) value);
                    user.setModifiedAt(System.currentTimeMillis());
                } else if(fieldName.equals("email")) {
                    if(value instanceof String) user.setEmail((String) value);
                    user.setModifiedAt(System.currentTimeMillis());
                } else if (fieldName.equals("gender")) {
                    if(value instanceof Character) user.setGender((Character) value);
                    user.setModifiedAt(System.currentTimeMillis());
                } else if (fieldName.equals("grade")) {
                    if(value instanceof Integer) user.setGrade((Integer) value);
                    user.setModifiedAt(System.currentTimeMillis());
                }
        });
    }

    @Override
    public void deleteUser(UUID id) {
        Iterator<User> iterator = data.iterator();
        while(iterator.hasNext()) {
            User user = iterator.next();
            if(user.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public Optional<User> findByUId(UUID id) {
        return Optional.empty();
    }

    //JCF User 저장 로직
    public void saveUser(User user) {
        user.setModifiedAt(user.getCreatedAt());
        data.add(userRepository.save(user));
    }
}
