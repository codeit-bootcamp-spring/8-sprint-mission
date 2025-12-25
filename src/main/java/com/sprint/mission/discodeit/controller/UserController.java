package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //  유저 생성 (포스트맨 POST /users 테스트용)
    @PostMapping
    public UserResponse create(@RequestBody UserCreateRequest request) {
        return userService.create(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getProfileImage() // 멘토 피드백: 프로필 이미지 포함
        );
    }

    //  모든 유저 조회
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    //  유저 정보 수정 (멘토 피드백: 선택적 프로필 이미지 교체 기능)
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return userService.update(
                id,
                request.getName(),
                request.getPassword(),
                request.getProfileImage() // 멘토 피드백 반영
        );
    }

    //  유저 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}