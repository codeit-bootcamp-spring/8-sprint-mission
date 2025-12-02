package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dummy.DummyData;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

public class JCFUserService implements UserService {

    private final Map<UUID, User> data = new HashMap<>();

    // 유저 생성
    @Override
    public User create(String name, String gender, Integer age) {
        User user = new User(name, gender, age);
        data.put(user.getId(), user);
        return user;
    }

    // 유저 조회 (단건)
    @Override
    public User findUser(UUID id) {
        return data.get(id);
    }

    // 유저 조회 (다건)
    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }


    // 성별로 유저 조회
    @Override
    public List<User> findAllGender(String gender) {
            return data.values().stream()
                    .filter(user -> Objects.equals(user.getGender(), gender))
                    .collect(Collectors.toList());

    }

    // 나이대 별 정렬 조회
    @Override
    public List<User> findAllAge() {
        return data.values().stream()
                .sorted(Comparator.comparing(User::getAge))
                .collect(Collectors.toList());
    }

    // 유저 수정
    @Override
    public User update(UUID id, User updateUser) {
        User user = findUser(id);

        if (user == null) {
            throw new IllegalArgumentException("해당 유저가 존재하지 않습니다.");
        }

        user.update(updateUser);
        return user;
    }

    // 유저 삭제
    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
