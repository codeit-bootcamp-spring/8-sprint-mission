package com.sprint.mission.discodeit.service.file; // 패키지명은 프로젝트 구조에 따라 다를 수 있습니다.

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository; // Repository import
import com.sprint.mission.discodeit.repository.file.FileUserRepository; // 기본 초기화용 import
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileUserService implements UserService {

    // 1. Repository 필드 선언 (의존성 주입 대상)
    private final UserRepository userRepository;

    // 2. Repository를 주입받는 생성자 (DI)
    public FileUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 3. (선택적) DI를 사용하지 않을 경우를 위한 기본 생성자
    public FileUserService() {
        this.userRepository = FileUserRepository.getInstance();
    }

    // --- Service 인터페이스 구현 (update 메서드 추가) ---

    @Override
    public User create(String name, String email) {
        // 1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(name, "이름");
        ValidationUtil.validateNotNullOrEmpty(email, "이메일");

        // 2. Entity 객체 생성
        User newUser = new User(name, email);

        // 3. Repository에 저장 요청
        return userRepository.save(newUser);
    }

    @Override
    public User update(UUID userId, String newName, String newEmail) {
        //  1. 유효성 검사 (Service 책임)
        ValidationUtil.validateNotNullOrEmpty(newName, "새 이름");
        ValidationUtil.validateNotNullOrEmpty(newEmail, "새 이메일");

        // 2. 대상 Entity를 Repository에서 조회
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 사용자 ID를 찾을 수 없습니다: " + userId));

        // 3. Entity의 상태 변경 메서드 호출 (User.java에 update(String, String) 메서드가 있어야 함)
        userToUpdate.update(newName, newEmail);

        // 4. Repository에 수정된 Entity 저장
        return userRepository.save(userToUpdate);
    }

    // 나머지 findById, findAll, delete 메서드는 userRepository를 호출하도록 유지
    @Override
    public Optional<User> findById(UUID id) { return userRepository.findById(id); }

    @Override
    public List<User> findAll() { return userRepository.findAll(); }

    @Override
    public void delete(UUID id) { userRepository.delete(id); }
}