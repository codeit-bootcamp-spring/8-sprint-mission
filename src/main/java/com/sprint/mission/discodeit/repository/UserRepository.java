package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface UserRepository extends JpaRepository<User, UUID> {

  @EntityGraph(attributePaths = {"profile", "userStatus"})
  Optional<User> findByUsername(String username);

  @Query("SELECT u FROM User u JOIN FETCH u.userStatus")
  List<User> findAll();

  boolean existsByUsername(String name);

  boolean existsByEmail(String email);
}
