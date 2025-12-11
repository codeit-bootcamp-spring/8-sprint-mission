package com.sprint.mission.discodeit.service.impl;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.util.ValidationUtil; // 유효성 검사 유틸리티 가정
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    // Singleton 패턴 가정
    private final UserRepository userRepository = FileUserRepository.getInstance();

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
                .orElseThrow(() -> new IllegalArgumentException("수정할 사용자를 찾을 수 없습니다: " + userId));

        // ✨ 3. Entity의 상태 변경 메서드 호출
        userToUpdate.update(newName, newEmail);

        // 4. Repository에 수정된 Entity 저장 (save 메서드 재활용)
        return userRepository.save(userToUpdate);
    }

    // 나머지 조회 및 삭제 메서드는 변경 없이 유지
    @Override
    public Optional<User> findById(UUID id) { return userRepository.findById(id); }

    @Override
    public List<User> findAll() { return userRepository.findAll(); }

    @Override
    public void delete(UUID id) { userRepository.delete(id); }
}