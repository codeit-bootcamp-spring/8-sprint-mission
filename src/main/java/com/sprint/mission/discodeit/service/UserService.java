package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // 유저 생성
    User create(String name, String gender, Integer age);
    
    // 유저 조회 (단건)
    User findUser(UUID id);

    // 유저 조회 (다건)
    List<User> findAll();

    // 성별 별 조회
    List<User> findAllGender(String gender);

    // 나이대 별 조회
    List<User> findAllAge();

    // 수정
    User update(UUID id, User updateUser);
    
    // 삭제
    void delete(UUID id);

    
}
