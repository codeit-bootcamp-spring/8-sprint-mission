package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [심화] 모든 사용자 조회
     * 브라우저(user-list.html)의 500 에러를 방지하기 위해 필드 구성을 단순화했습니다.
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        try {
            List<UserDto> userDtos = userService.findAll().stream()
                    .map(response -> new UserDto(
                            response.getId(),
                            // 화면 소스가 'username'을 기대하므로 response.getName()을 매핑
                            response.getName() != null ? response.getName() : "Unknown",
                            response.getEmail() != null ? response.getEmail() : "",
                            // Postman에서 확인된 profileId UUID 전달
                            response.getProfileId(),
                            response.isOnline()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            // 에러 발생 시 서버 콘솔에 원인 출력
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}