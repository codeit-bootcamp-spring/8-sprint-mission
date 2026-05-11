package com.sprint.mission.discodeit.config.webSocket;

import java.security.Principal;
import java.util.Objects;

/**
 * STOMP 세션에 바인딩되는 사용자 식별 Principal 구현체
 * WebSocket CONNECT 프레임의 userId 헤더를 Spring Messaging의 Principal 체계로 전달하여
 * 이후 @MessageMapping 메서드에서 Principal 파라미터로 안전하게 사용자 정보를 참조할 수 있도록 한다.
 *
 * 학습 포인트:
 * - Spring Security 없이도 STOMP 레벨에서 간단한 인증/식별 개념을 시연할 수 있다
 * - WebSocketInterceptor의 preSend()에서 accessor.setUser(...)로 이 객체를 주입한다
 */
public class StompPrincipal implements Principal {

    private final String userId;

    public StompPrincipal(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId는 비어있을 수 없다");
        }
        this.userId = userId.trim();
    }

    /**
     * Principal의 표준 식별자 반환 메서드
     * Spring은 내부적으로 이 값을 사용자 구분 키로 사용한다
     *
     * @return 사용자 ID
     */
    @Override
    public String getName() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StompPrincipal that = (StompPrincipal) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "StompPrincipal{userId='" + userId + "'}";
    }
}
