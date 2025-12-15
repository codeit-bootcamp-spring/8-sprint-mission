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
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public boolean existsById(UUID id) {
        return data.containsKey(id);
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
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return data.values().stream()
                .filter(u -> Objects.equals(u.getName(), username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return data.values().stream()
                .filter(u -> Objects.equals(u.getEmail(), email))
                .findFirst();
    }

    // 비행기에 탑승하는 100명의 승객 중 단 한명이라도 금지 물품(조건)을 소지했는지 검사
    // 만약 10번째 인물이 금지 물품 소지했으면 물품소지자 발견 ! 하면서 -> 검사 중단. -> true 반환
    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        return data.values().stream()
                .anyMatch(u -> Objects.equals(u.getName(), username)
                || Objects.equals(u.getEmail(), email));
    }
}
