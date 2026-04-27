package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"profile"})
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String name);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u "
            + "LEFT JOIN FETCH u.profile")
    List<User> findAllWithProfile();
}
