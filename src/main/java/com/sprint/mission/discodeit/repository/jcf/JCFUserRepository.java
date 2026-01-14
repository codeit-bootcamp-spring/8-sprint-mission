package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFUserRepository implements UserRepository {

  private final Map<UUID, User> data;

  public JCFUserRepository() {
        /*
        //기존에 사용했던 HashMap은 모든 HTTP 요청이 같은 HashMap에 접근하기 때문에 Tread-Safe 하지 못함
        //기본적으로 @Repository scope는 Singleton
        //Tread-Safe한 ConcurrentHashMap사용
        */
    this.data = new ConcurrentHashMap<>();
  }

  @Override
  public User save(User user) {
    data.put(user.getId(), user);
    return user;
  }

  @Override
  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(this.data.get(id));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return data.values().stream()
        .filter(u -> u.getUsername().equals(username)).findFirst();
  }

  @Override
  public List<User> findAll() {
    return data.values().stream().toList();
  }

  @Override
  public void delete(UUID id) {
    this.data.remove(id);
  }

  @Override
  public boolean existsByName(String name) {
    return data.values().stream().anyMatch(u -> u.getUsername().equals(name));
  }

  @Override
  public boolean existsByEmail(String email) {
    return data.values().stream().anyMatch(u -> u.getEmail().equals(email));
  }
}
