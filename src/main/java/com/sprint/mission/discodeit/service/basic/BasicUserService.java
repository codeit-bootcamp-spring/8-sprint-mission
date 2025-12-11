package com.sprint.mission.discodeit.service.basic; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository; //  Repository import
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// UserService 인터페이스 구현
public class BasicUserService implements UserService {

    // 1. Repository 필드 선언 (의존성 주입 대상)
    private final UserRepository userRepository;

    // 2. Repository를 주입받는 생성자 (DI)
    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- Service 인터페이스 구현 (책임 분리 및 유효성 검사 반영) ---

    @Override
    public User create(String name, String email) {
        // 1. 유효성 검사 (Service 책임)
        ValidationUtil.validateNotNullOrEmpty(name, "이름");
        ValidationUtil.validateNotNullOrEmpty(email, "이메일");

        // 2. Entity 객체 생성 (Service 책임)
        User newUser = new User(name, email);

        // 3. Repository에 저장 요청
        return userRepository.save(newUser);
    }

    @Override
    public User update(UUID userId, String newName, String newEmail) {
        //  1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(newName, "새 이름");
        ValidationUtil.validateNotNullOrEmpty(newEmail, "새 이메일");

        // 2. 대상 Entity를 Repository에서 조회
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 사용자 ID를 찾을 수 없습니다: " + userId));

        //  3. Entity의 상태 변경 메서드 호출 (User.java에 update(String, String) 메서드가 있어야 함)
        userToUpdate.update(newName, newEmail);

        // 4. Repository에 수정된 Entity 저장 (save 메서드 재활용)
        return userRepository.save(userToUpdate);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        // 삭제 로직: ID로 존재 여부를 확인하고 삭제하는 로직이 Repository 내부에 있다고 가정
        userRepository.delete(id);
    }
}