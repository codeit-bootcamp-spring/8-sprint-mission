package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    // 유저 생성 (GET-only 미션 대응)
    @RequestMapping("create")
    public ResponseEntity<UserResponse> create(
            @ModelAttribute("userCreateRequest") UserCreateRequest userCreateRequest,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) {
        // 파일이 존재할 경우만 -> DTO로 변환 -> 서비스 전달
        BinaryContentCreateRequest profileImgRequest = convertProfileInfo(profileImg);

        UserResponse response = userService.create(userCreateRequest, profileImgRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 유저 수정 (GET-only 미션 대응)
    @RequestMapping("update")
    public ResponseEntity<UserResponse> update(@ModelAttribute("userUpdateRequest") UserUpdateRequest userUpdateRequest,
                                               @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) {
        BinaryContentCreateRequest profileImgRequest = convertProfileInfo(profileImg);

        UserResponse response = userService.update(userUpdateRequest, profileImgRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 유저 삭제 (GET-only 미션 대응)
    @RequestMapping("delete")
    public ResponseEntity<Void> delete(@RequestParam("userId") UUID userId) {
        userService.delete(userId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    // 유저 다건 조회 (GET-only 미션 대응)
    @RequestMapping("findAll")
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> findList = userService.findAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(findList);
    }

    // 사용자 온라인 상태 업데이트 (GET-only 미션 대응)
    @RequestMapping("update-online")
    public ResponseEntity<UserStatusResponse> updateOnline(
            @ModelAttribute UserStatusUpdateRequest request
    ) {
        UserStatusResponse updateUserStatus = userStatusService.updateByUserId(request.id(), request.lastConnAt());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updateUserStatus);
    }

    // MultipartFile -> BinaryContentCreateRequest DTO로 변환 메서드
    private BinaryContentCreateRequest convertProfileInfo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지를 처리할 수 없습니다.", e);
        }
    }


}
