package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {

    // 유저 생성
    User save(User user);

    // 유저 조회 (단건)
    User findById(UUID id);

    // 유저 조회 (다건)
    List<User> findAll();

    // 유저 조회 (특정 성별 조회)
    List<User> findAllGender(String gender);

    // 유저 조회 (나이대별 조회)
    List<User> findAllAge();

    // 유저 수정
    User update(UUID id, User updateUser);

    // 유저 삭제
    void delete(UUID id);
}
