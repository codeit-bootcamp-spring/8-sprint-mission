package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.security.jwt.store.JwtRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Component
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider tokenProvider;
    private final JwtRegistry jwtRegistry;

    /**
     * 로그아웃 핸들러 생성자.
     *
     * @param tokenProvider 만료된 리프레시 쿠키를 생성하기 위한 프로바이더
     */
    public JwtLogoutHandler(JwtTokenProvider tokenProvider,
                            JwtRegistry jwtRegistry) {
        log.info("[JwtLogoutHandler] 생성자 호출됨: 만료된 리프레시 쿠키 생성 + 세션 레지스트리 주입");
        this.tokenProvider = tokenProvider;
        this.jwtRegistry = jwtRegistry;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        log.info("[JwtLogoutHandler] 로그아웃 처리 시작: 리프레시 쿠키 만료 응답 추가");

        Cookie refreshTokenExpirationCookie = tokenProvider.generateRefreshTokenExpirationCookie();
        response.addCookie(refreshTokenExpirationCookie);

        Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst()
                .ifPresent(cookie -> {
                    String refreshToken = cookie.getValue();
                    UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
                    jwtRegistry.invalidateJwtInformationByUserId(userId);
                });

//        if (request.getCookies() != null) {
//            Arrays.stream(request.getCookies())
//                    .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
//                    .findFirst()
//                    .ifPresent(cookie -> {
//                        String refreshToken = cookie.getValue();
//
//                        // Provider를 통해 userId를 추출(캡슐화)
//                        UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
//
//                        if (userId != null) {
//                            jwtRegistry.invalidateJwtInformationByUserId(userId);
//                            log.info("[JwtLogoutHandler] RT 무효화 완료: userId={}", userId);
//                        } else {
//                            jwtRegistry.invalidateByRefreshToken(refreshToken);
//                            log.warn("[JwtLogoutHandler] 유저 ID 파싱 실패. 해당 리프레시 토큰 단건 무효화 처리");
//                        }
//                    });
//        }

        // 리프레시 토큰을 즉시 만료시키는 쿠키를 응답에 추가한다 (클라이언트가 보관한 RT 제거)

        log.info("[JwtLogoutHandler] 로그아웃 처리 완료: 리프레시 토큰 쿠키 정리됨");
    }
}
