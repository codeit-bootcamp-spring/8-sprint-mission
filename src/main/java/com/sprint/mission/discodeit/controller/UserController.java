package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserStatusRequest;
import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    /**
     * 모든 사용자 조회
     * GET /api/users
     */
    @RequestMapping(method = RequestMethod.GET)
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

    /**
     * User 등록
     * POST /api/users
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    /**
     * User 정보 수정
     * PATCH /api/users/{userId}
     */
    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    public ResponseEntity<UserResponse> update(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    /**
     * User 삭제
     * DELETE /api/users/{userId}
     */
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * User 온라인 상태 업데이트
     * PATCH /api/users/{userId}/userStatus
     * 
     * [활용 방법 및 호출 시점]
     * 1. 클라이언트 앱 실행 시: 앱이 백그라운드에서 포그라운드로 전환될 때 호출하여 사용자를 온라인 상태로 만듭니다.
     * 2. 주기적 하트비트: 앱이 활성화된 동안 일정 간격(예: 1분)마다 호출하여 사용자의 온라인 상태를 유지합니다.
     * 3. 앱 종료 시: 앱이 완전히 종료되기 전 호출할 수 있으나, 서버 측에서 일정 시간(예: 5분) 동안 
     *    업데이트가 없으면 자동으로 오프라인으로 처리하는 것이 더 안전합니다.
     * 
     * [클라이언트 앱에서의 반영]
     * 1. 다른 사용자의 목록 화면: 해당 사용자의 온라인/오프라인 상태 배지가 실시간으로 업데이트됩니다.
     *    (예: "사용자 목록" 화면에서 초록색 "온라인" 배지 표시)
     * 2. 채팅 목록: 각 채널에서 상대방의 온라인 상태가 표시되어 현재 대화 가능 여부를 알 수 있습니다.
     * 3. 실시간 상태 동기화: 클라이언트 앱은 주기적으로 사용자 목록 API를 호출하거나 WebSocket을 통해
     *    상태 변경을 받아 화면에 반영합니다.
     * 
     * [기술적 고려사항]
     * - lastAccessAt 필드가 현재 시간으로 업데이트됩니다.
     * - 서버에서 isOnline() 메서드는 lastAccessAt 기준으로 5분(300초) 이내 접근 시 온라인으로 판단합니다.
     * - 이를 통해 네트워크 오류나 앱 크래시로 인한 상태 업데이트 실패를 자동으로 처리할 수 있습니다.
     */
    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusResponse> updateStatus(@PathVariable UUID userId, @RequestBody UserStatusRequest request) {
        // 경로 파라미터의 userId를 사용하여 UserStatusRequest 생성
        UserStatusRequest statusRequest = new UserStatusRequest(userId);
        UserStatusResponse response = userStatusService.update(statusRequest);
        return ResponseEntity.ok(response);
    }
}