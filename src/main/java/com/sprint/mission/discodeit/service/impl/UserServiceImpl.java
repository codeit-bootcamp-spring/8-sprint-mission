package com.sprint.mission.discodeit.service.impl;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository; // DI가 아닐 때를 대비한 import
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.ValidationUtil; // 유효성 검사 유틸리티 import
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    // 1. Repository 필드 선언
    private final UserRepository userRepository;

    // 2. Repository를 주입받는 생성자 (DI)
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 3. (선택적) DI를 사용하지 않을 경우를 위한 기본 생성자 (File 구현체 사용)
    public UserServiceImpl() {
        // 실제 애플리케이션에서는 이 생성자보다는 DI를 사용하는 것이 권장됨
        this.userRepository = FileUserRepository.getInstance();
    }

    // --- Service 인터페이스 구현 ---

    @Override
    public User create(String name, String email) {
        //  1. 유효성 검사 (Service 책임)
        ValidationUtil.validateNotNullOrEmpty(name, "이름");
        ValidationUtil.validateNotNullOrEmpty(email, "이메일");

        //  2. Entity 객체 생성 (Service 책임)
        User newUser = new User(name, email);

        // 3. Repository에 저장 요청
        return userRepository.save(newUser);
    }

    @Override
    public User update(UUID userId, String newName, String newEmail) {
        // 1. 유효성 검사
        ValidationUtil.validateNotNullOrEmpty(newName, "새 이름");
        ValidationUtil.validateNotNullOrEmpty(newEmail, "새 이메일");

        // 2. 대상 Entity를 Repository에서 조회
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 사용자 ID를 찾을 수 없습니다: " + userId));

        //  3. Entity의 상태 변경 메서드 호출 (Entity의 update 메서드 사용)
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
        // 삭제할 엔티티가 있는지 먼저 확인하는 로직 추가 가능
        userRepository.delete(id);
    }
}