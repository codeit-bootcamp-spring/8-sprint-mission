package com.sprint.mission.discodeit.service.jcf; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.NoSuchElementException; // 예외 처리용 import

// UserService 인터페이스 구현
public class JCFUserService implements UserService {

    // 1. JCF (Map) 필드 선언 (메모리 저장소)
    private final Map<UUID, User> data;

    // 2. 생성자
    public JCFUserService() {
        this.data = new HashMap<>();
    }

    // --- Service 인터페이스 구현 (update 메서드 추가) ---

    @Override
    public User create(String name, String email) {
        // 1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(name, "이름");
        ValidationUtil.validateNotNullOrEmpty(email, "이메일");

        // 2. Entity 객체 생성
        User newUser = new User(name, email);

        // 3. JCF Map에 저장
        data.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User update(UUID userId, String newName, String newEmail) {
        //  1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(newName, "새 이름");
        ValidationUtil.validateNotNullOrEmpty(newEmail, "새 이메일");

        // 2. 대상 Entity를 JCF Map에서 조회
        User userToUpdate = Optional.ofNullable(data.get(userId))
                .orElseThrow(() -> new NoSuchElementException("수정할 사용자 ID를 찾을 수 없습니다: " + userId));

        // 3. Entity의 상태 변경 메서드 호출 (User.java에 update(String, String) 메서드가 있어야 함)
        userToUpdate.update(newName, newEmail);

        // 4. JCF Map에 다시 저장 (참조형이므로 사실상 Map의 객체가 직접 수정됨)
        return userToUpdate;
    }

    // 나머지 findById, findAll, delete 메서드

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<User> findAll() {
        return data.values().stream().collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (!data.containsKey(id)) {
            throw new NoSuchElementException("삭제할 사용자 ID를 찾을 수 없습니다: " + id);
        }
        data.remove(id);
    }
}