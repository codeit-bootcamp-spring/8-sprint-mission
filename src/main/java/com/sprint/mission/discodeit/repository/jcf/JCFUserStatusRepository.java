package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
public class JCFUserStatusRepository implements UserStatusRepository {
    private final Map<UUID, UserStatus> data;

    public JCFUserStatusRepository() {
        /*
        //기존에 사용했던 HashMap은 모든 HTTP 요청이 같은 HashMap에 접근하기 때문에 Tread-Safe 하지 못함
        //기본적으로 @Repository scope는 Singleton
        //Tread-Safe한 ConcurrentHashMap사용
        */
        this.data = new ConcurrentHashMap<>();
    }


    @Override
    public UserStatus save(UserStatus userStatus) {
        data.put(userStatus.getId(), userStatus);
        return userStatus;
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return data.values().stream().anyMatch(userStatus -> userStatus.getUserId().equals(userId));
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return data.values().stream().filter(userStatus -> userStatus.getUserId().equals(userId)).findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return data.values().stream().toList();
    }


    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        data.values().removeIf(userStatus -> userStatus.getUserId().equals(userId));
    }
}
