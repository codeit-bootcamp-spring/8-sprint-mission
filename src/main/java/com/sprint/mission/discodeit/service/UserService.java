package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    // 유저 생성
    void createUser(User user);

    User create(String userId, String name, String email, char gender, int grade);

    // 단건 조회
    User findById(UUID id);

    //다건 조회
    //- 전체 조회
    List<User> findUsers();

    //- 학년별 조회
    Map<Integer, List<User>> findUserByGrade();

    //유저 수정
    void updateUser(UUID id, String fieldName, Object value);

    //유저 삭제
    void deleteUser(UUID id);

    //유저 조회(FileUserRepository)
    Optional<User> findByUId(UUID id);

    //유저 저장
    void saveUser(User user);
}
