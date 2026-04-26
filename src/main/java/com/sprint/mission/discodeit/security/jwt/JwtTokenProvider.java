package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    private final JwtProperties jwtProperties;

    @PostConstruct
    public void init() {
        if (jwtProperties.secret().getBytes().length < 32) {
            throw new IllegalArgumentException("JWT Secret Key must be at least 32 bytes (256 bits) for HS256 algorithm.");
        }
    }

    // SecretKey 객체를 매번 바이트 배열로 생성하지 않고 재사용하기 위해 Signer/Verifier를 필드로 두거나
    // 키 자체를 캐싱할 수 있지만, 여기서는 직관성을 위해 유지하되 로직만 다듬었습니다.

    public String createAccessToken(String username) {
        return createToken(username, jwtProperties.accessExpirationMs());
    }

    public String createRefreshToken(String username) {
        return createToken(username, jwtProperties.refreshExpirationMs());
    }

    private String createToken(String username, long expirationMs) {
        try {
            // 수정 포인트 1: HMAC SHA 서명 시 secret의 바이트 길이를 검증하는 것이 좋습니다.
            // MACSigner는 HS256 기준 32바이트(256비트) 이상의 키를 권장합니다.
            JWSSigner signer = new MACSigner(jwtProperties.secret().getBytes());

            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issuer("discodeit-auth-server") // 수정 포인트 2: 발급자(iss) 추가 (보안 권장사항)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusMillis(expirationMs)))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), // 수정 포인트 3: Header에 Type 명시
                    claimsSet
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("JWT 토큰 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("JWT 토큰을 생성할 수 없습니다.", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(jwtProperties.secret().getBytes());

            // 서명 검증
            if (!signedJWT.verify(verifier)) {
                log.warn("JWT 서명이 유효하지 않습니다.");
                return false;
            }

            // 수정 포인트 4: 만료 시간 검증 시 '시간차(Clock Skew)' 허용 고려
            // 서버 간 시간 오차가 발생할 수 있으므로 약 1분 정도의 여유를 주는 것이 실무적입니다.
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                log.warn("JWT 토큰이 만료되었습니다.");
                return false;
            }

            return true;
        } catch (ParseException | JOSEException e) {
            log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            // 수정 포인트 5: 검증되지 않은 토큰을 파싱만 해서 subject를 꺼내는 것은 위험할 수 있습니다.
            // 실제 필터에서는 validateToken을 먼저 호출한 뒤 이 메서드를 쓰도록 가이드하세요.
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.error("JWT 토큰 파싱 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("JWT 토큰에서 정보를 추출할 수 없습니다.", e);
        }
    }

    public Instant getExpirationTimeFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null ? expirationTime.toInstant() : Instant.MIN;
        } catch (ParseException e) {
            log.warn("토큰에서 만료 시간을 파싱할 수 없습니다.", e);
            throw new RuntimeException("토큰 파싱 실패", e);
        }
    }
}