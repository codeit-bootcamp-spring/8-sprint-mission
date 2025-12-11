package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    // 생성 책임: 필수 인자값을 받아 새로운 User 객체를 생성 후 저장
    User create(String name, String email);

    // 수정 책임: 수정 대상 ID와 변경할 인자값을 받아 User를 수정
    User update(UUID userId, String newName, String newEmail);

    Optional<User> findById(UUID id);
    List<User> findAll();
    void delete(UUID id);
}