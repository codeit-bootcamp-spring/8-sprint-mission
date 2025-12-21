package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFUserStatusRepository implements UserStatusRepository {
    private final List<UserStatus> list = new ArrayList<>();

    @Override
    public UserStatus save(UserStatus userStatus) {
        list.removeIf(e -> e.getId().equals(userStatus.getId()));
        list.add(userStatus);
        return userStatus;
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return list.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(list);
    }

    @Override
    public void delete(UUID id) {
        list.removeIf(e -> e.getId().equals(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return list.stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst();
    }
}