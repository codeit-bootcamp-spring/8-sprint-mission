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

    @RequestMapping(method = RequestMethod.POST)
    public UserResponse create(@RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserResponse findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public UserResponse update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        // 경로의 id와 DTO의 id가 다를 경우를 대비해 DTO를 보정하거나 서비스 로직에서 처리합니다.
        return userService.update(request);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}