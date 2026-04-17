package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.security.jwt.store.JwtTokenEntity;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

/**
 * Access/Refresh 토큰 생성 및 검증을 담당하는 Bean 클래스
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // 리프레시 토큰을 저장할 HTTP 쿠기의 이름
    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH-TOKEN";

    // 엑세스 토큰의 만료 시간(밀리초 단위)
    private final int accessTokenExpirationMs;
    // 리프레시 토큰의 만료 시간(밀리초 단위)
    private final int refreshTokenExpirationMs;

    // 엑세스 토큰을 서명하기 위한 서명자
    private final JWSSigner accessTokenSigner;
    // 엑세스 토큰의 서명을 검증하기 위한 검증자
    private final JWSVerifier accessTokenVerifier;
    // 리프레시 토큰을 서명하기 위한 서명자
    private final JWSSigner refreshTokenSigner;
    // 리프레시 토큰의 서명을 검증하기 위한 검증자
    private final JWSVerifier refreshTokenVerifier;

    /**
     * 구성 프로퍼티를 기반으로 토큰 서명/검증자와 만료 시간을 초기화한다.
     * 이 생성자는 애플리케이션 시작 시 한 번 호출되며, 이후 발금/검증 로직에서 재사용된다.
     *
     * @param accessTokenSecret        Access 토큰에 사용할 HMAC 비밀키(HS256)
     * @param accessTokenExpirationMs  Access 토큰 만료 시간(ms)
     * @param refreshTokenSecret       Refresh 토큰에 사용할 HMAC 비밀키(HS256)
     * @param refreshTokenExpirationMs Refresh 토큰 만료 시간
     * @throws JOSEException 서명자/검증자 초기화 실패 시 발생
     */
    public JwtTokenProvider(
            @Value("${jwt.access-token.secret}") String accessTokenSecret,
            @Value("${jwt.access-token.exp}") int accessTokenExpirationMs,
            @Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
            @Value("${jwt.refresh-token.exp}") int refreshTokenExpirationMs
    ) throws JOSEException {

        log.info("[TokenProvider] 생성자 호출됨: 토큰 서명/검증자 및 만료 시간 초기화");

        // 주입받은 만료 시간 값들을 필드에 저장하여 토큰 생성 시 사용할 수 있도록 설정한다.
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;

        // 엑세스 토큰용 비밀키를 바이트 배열로 변환하여 HMAC-SHA256 서명자와 검증자를 생성한다.
        // 이를 통해 엑세스 토큰의 무결성을 보장하고 위변조를 방지할 수 있다.
        // Access 토큰 검증/서명을 위한 비밀키 바이트 배열을 준비한다.
        byte[] accessSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenSigner = new MACSigner(accessSecretBytes);
        this.accessTokenVerifier = new MACVerifier(accessSecretBytes);

        // 리프레시 토큰용 비밀키를 바이트 배열로 변환하여 별도의 서명자와 검증자를 생성한다.
        // 엑세스 토큰과 다른 비밀키를 사용함으로써 각 토큰의 독립적인 보안성을 확보한다.
        byte[] refreshSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);
        this.refreshTokenSigner = new MACSigner(refreshSecretBytes);
        this.refreshTokenVerifier = new MACVerifier(refreshSecretBytes);
    }

    /**
     * 엑세스 토큰을 생성한다.
     * 로그인 성공 또는 리프레시 토큰을 통한 재발급 시 호출되며,
     * 단기 인증에 사용되는 짧은 수명의 토큰을 발급한다.
     *
     * @param userDetails 사용자 정보(아이디, 권한, 내부 식별자)
     * @return 직렬화된 JWT 문자열(Access Token)
     * @throws JOSEException 토큰 서명 과정에서 실패할 경우 발생한다.
     */
    public String generateAccessToken(DiscodeitUserDetails userDetails) throws JOSEException {

        log.info("[TokenProvider] generateAccessToken 호출됨: {}의 엑세스 토큰 생성", userDetails.getUsername());

        // 생성할 토큰의 타입이 "access"인 경우 엑세스 토큰을 생성한다.
        return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, "access");
    }

    /**
     * 리프레시 토큰을 생성한다.
     * 로그인 성공 또는 리프레시 토큰 회전(rotation) 정책에 따라 새 RT를 발급할 때 호출된다.
     * 이 토큰은 쿠키(HttpOnly)에 저장되어 엑세스 토큰 재발급 시도에 사용된다.
     *
     * @param userDetails 사용자 정보(아이디, 권한, 내부 식별자)
     * @return 직렬화된 JWT 문자열(Refresh Token)
     * @Throws JOSEException 토큰 서명 과정에서 실패할 경우 발생한다.
     */
    public String generateRefreshToken(DiscodeitUserDetails userDetails) throws JOSEException {

        log.info("[TokenProvider] generateRefreshToken 호출됨: {}의 리프레시 토큰 생성", userDetails.getUsername());

        // 생성할 토큰의 타입이 "refresh"인 경우 리프레시 토큰을 생성한다.
        return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, "refresh");
    }

    /**
     * 토큰 생성
     *
     * @param userDetails  사용자 정보
     * @param expirationMs 토큰 만료 시간
     * @param signer       토큰 서명자
     * @param tokenType    토큰 타입("access" 또는 "refresh")
     * @return 생성된 토큰
     * @throws JOSEException 토큰 생성 중 발생할 수 있는 예외
     */
    private String generateToken(DiscodeitUserDetails userDetails, int expirationMs, JWSSigner signer,
                                 String tokenType) throws JOSEException {

        log.info("[TokenProvider] generateToken: {}의 {} 토큰 생성 시작", userDetails.getUsername(), tokenType);

        // 토큰의 고유 식별자(jti)로 사용할 랜덤 UUID를 생성한다.
        String tokenId = UUID.randomUUID().toString();

        // 현재 시간을 기준으로 토큰의 만료 시간을 설정한다.
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        // 토큰의 클레임(claims)을 설정한다.
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                // 토큰 주체(sub)
                .subject(userDetails.getUsername())
                // 토큰 고유 식별자(jti)
                .jwtID(tokenId)
                // 사용자 ID(userId)
                .claim("userId", userDetails.getUserDto().id())
                // 토큰 타입(type)
                .claim("type", tokenType)
                // 사용자 권한(roles)
                .claim("roles",
                        userDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
                // 토큰 발급 시간(iat)
                .issueTime(now)
                // 토큰 만료 시간(exp)
                .expirationTime(expiryDate)
                .build();

        // 토큰 생성: 준비된 클레임과 헤더(HS256)를 사용하여 토큰 생성.
        // 아직 서명되지 않은 토큰이다.
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        // 토큰 서명: 생성된 토큰에 서명자를 적용하여 서명.
        signedJWT.sign(signer);

        // 토큰 직렬화: 실질적으로 JWT 토큰을 생성한 후 URL 안전한 문자열(Base64 인코딩)로 직렬화하는 메서드.
        String completedJWT = signedJWT.serialize();

        // 토큰 값 대신 토큰 ID(jti)나 마스킹된 값만 로깅
        log.info("[TokenProvider] generateToken: {} 토큰 생성 완료: jti={}", tokenType, tokenId);

        return completedJWT;
    }

    /**
     * 리프레시 토큰을 HttpOnly 쿠키로 생성한다.
     * 로그인 성공 또는 리프레시 성공 시 브라우저로 내려보낼 때 사용된다.
     *
     * @param refreshToken 직렬화된 JWT 문자열
     * @return HttpOnly 설정이 적용된 쿠키 인스턴스
     */
    public Cookie generateRefreshTokenCookie(String refreshToken) {

        log.info("[TokenProvider] generateRefreshTokenCookie 호출됨: Refresh Token 쿠키 생성");

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발 환경: HTTP도 동작하도록 Secure=false (운영 환경은 true를 사용해 HTTPS 통신을 이용할 수 있도록 권장)
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenExpirationMs / 1000);

        log.info("[TokenProvider] generateRefreshTokenCookie 완료: Max-Age=({})", refreshTokenExpirationMs / 1000);

        return cookie;
    }

    /**
     * 리프레시 토큰 쿠키를 즉시 만료시키는 쿠키를 생성한다.
     * 로그아웃이나 보안 이벤트 발생 시 클라이언트 보유 RT를 제거하기 위해 사용한다.
     *
     * @return Max-Age=0으로 설정된 만료 쿠키
     */
    public Cookie generateRefreshTokenExpirationCookie() {

        log.info("[TokenProvider] generateRefreshTokenExpirationCookie 호출됨: Refresh Token 만료 쿠키 생성");

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");

        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 만료 시간을 0으로 설정하여 즉시 만료시킨다.

        log.info("[TokenProvider] generateRefreshTokenExpirationCookie 완료");

        return cookie;
    }

    /**
     * 리프레시 토큰을 담은 HttpOnly 쿠키를 응답에 추가한다.
     */
    public void addRefreshCookie(HttpServletResponse response, String refreshToken) {

        log.info("[TokenProvider] addRefreshCookie 호출됨: RT 쿠키 응답에 추가");
        Cookie cookie = generateRefreshTokenCookie(refreshToken);

        response.addCookie(cookie);
    }

    /**
     * 만료(삭제)용 리프레시 쿠키를 응답에 추가한다.
     * 재사용 차단이나 강제 로그아웃 시 사용.
     */
    public void expireRefreshCookie(HttpServletResponse response) {

        log.info("[TokenProvider] expireRefreshCookie 호출됨: 만료 쿠키 응답에 추가");
        Cookie cookie = generateRefreshTokenExpirationCookie();

        response.addCookie(cookie);
    }

    /**
     * 엑세스 토큰을 검증한다.
     * 보호된 API에 대한 요청 처리 직전에 호출되며,
     * 서명 무결성, 토큰 타입, 만료 여부를 순차적으로 검사한다.
     *
     * @param token 검사 대상 JWT 문자열
     * @return 유효하며 true, 그렇지 않으면 false
     */
    public boolean validateAccessToken(String token) {

        log.info("[TokenProvider] validateAccessToken 호출됨: 토큰 유효성 검사 시작");

        boolean result = verifyToken(token, accessTokenVerifier, "access");
        log.info("[TokenProvider] validateAccessToken 결과: {}", result);

        return result;
    }

    /**
     * 리프레시 토큰을 검증한다.
     * `/api/auth/refresh` 호출 시 쿠키에서 읽어온 토큰을 대상을 사용된다.
     * 서명 무결성, 토큰 타입, 만료 여부를 확인하여 재발급 가능 여부를 결정한다.
     *
     * @param token 검사 대상 JWT 문자열(쿠키에서 추출됨)
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateRefreshToken(String token) {

        log.info("[TokenProvider] validateRefreshToken 호출됨: 토큰 유효성 검사 시작");

        boolean result = verifyToken(token, refreshTokenVerifier, "refresh");
        log.info("[TokenProvider] validateRefreshToken 결과: {}", result);

        return result;
    }

    /**
     * 토큰의 서명과 클레임을 실제로 검증하는 내부 유틸리티 메서드이다.
     * 먼저 토큰을 파싱한 뒤, 제공된 검증자(HMAC)로 서명 무결성을 확인한다.
     * 이어서 `type` 클레임이 기대한 값과 일치하는지 검사하고, 마지막으로 만료 시간을 판정한다.
     *
     * @param token        검사 대상 JWT 문자열
     * @param verifier     서명 검증에 사용할 검증자(Access/Refresh별로 구분)
     * @param expectedType 기대하는 토큰 타입("access" 또는 "refresh")
     * @return 모든 조건을 충족하면 true, 하나라도 실패하면 false
     */
    private boolean verifyToken(String token, JWSVerifier verifier, String expectedType) {

        try {
            // 토큰 파싱
            log.info("[TokenProvider] verifyToken: 토큰 파싱 시작");
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 서명 무결성 검증
            log.info("[TokenProvider] verifyToken: 서명 무결성 검증 시작");
            if (!signedJWT.verify(verifier)) {
                log.info("[TokenProvider] verifyTOken: 서명 검증 실패");
                return false;
            }

            // 실제 값과 기대 값이 다른지 확인하는 로직
            String actualType = signedJWT.getJWTClaimsSet().getStringClaim("type");
            if (!expectedType.equals(actualType)) {
                log.warn("[TokenProvider] 토큰 타입 불일치: expected={}, actual={}", expectedType, actualType);
                return false;
            }

            // 만료 시간 검증
            log.info("TokenProvider] verifyToken: 만료 시간 검증 시작");
            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

            // 만료 시간이 null이 아니고, 현재 시간보다 이후인 경우 유효(true)
            boolean valid = exp != null && exp.after(new Date());

            // 위의 모든 검증이 성공하면 true, 하나라도 실패하면 false
            log.info("[TokenProvider] verifyToken: 만료 검사 결과=" + valid);

            return valid;
        } catch (Exception e) {
            log.info("[TokenProvider] verifyToken: 예외 발생- " + e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 주체(subject)로 저장된 사용자명을 추출한다.
     * 인증 필터가 사용자 정보를 로드하기 위해 호출한다.
     *
     * @param token JWT 문자열
     * @return 사용자명(subject)
     */
    public String getUsernameFromToken(String token) {
        try {
            log.info("[TokenProvider] getUsernameFromToken 호출됨: subject 추출 시작");

            SignedJWT signedJWT = SignedJWT.parse(token);
            String subject = signedJWT.getJWTClaimsSet().getSubject();

            log.info("[TokenProvider] getUsernameFormToken 결과: subject={}", subject);

            return subject;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public String getTokenId(String token) {
        try {
            log.info("[TokenProvider] getTokenId 호출됨: jti 추출 시작");

            SignedJWT signedJWT = SignedJWT.parse(token);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();

            log.info("[TokenProvider] getTokenId 결과: jti={}", jti);

            return jti;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * 토큰에서 발급 시간(iat)을 추출한다.
     * 디버깅이나 감사 로그에서 토큰 생성 시점을 확인할 때 유용하다.
     *
     * @param token JWT 문자열
     * @return 발급 시간(Date)
     */
    public Date getIssuedAt(String token) {
        try {
            log.info("[TokenProvider] getIssuedAt 호출됨: iat 추출 시작");

            SignedJWT signedJWT = SignedJWT.parse(token);
            Date iat = signedJWT.getJWTClaimsSet().getIssueTime();

            log.info("[TokenProvider] getIssuedAt 결과: iat={}", iat);

            return iat;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * 토큰에서 만료 시간(exp)을 추출한다.
     * 남은 유효 시간을 계산하거나 만료 임박 알림을 구현할 때 사용할 수 있다.
     *
     * @param token JWT 문자열
     * @return 만료 시간(Date)
     */
    public Date getExpiration(String token) {
        try {
            log.info("[TokenProvider] getExpiration 호출됨: exp 추출 시작");

            SignedJWT signedJWT = SignedJWT.parse(token);
            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

            log.info("[TokenProvider] getExpiration 결과: exp={}", exp);

            return exp;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    /**
     * 직렬화된 JWT 문자열을 파싱하여 `JwtTokenEntity`로 변환한다.
     * 컨트롤러나 핸들러 쪽에서 토큰의 메타데이터를 DB에 저장할 때 사용되는 유틸 메서드이다.
     *
     * @param token 직렬화된 JWT 문자열(Access 또는 Refresh)
     * @return 발급.만료 시각과 타입이 채워진 `JwtTokenEntity`
     */
    public JwtTokenEntity toEntity(String token) {
        try {
            log.info("[TokenProvider] toEntity 호출됨: 토큰 메타데이터 변환 시작");

            // 토큰 파싱
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 토큰 클레임 추출
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            String username = signedJWT.getJWTClaimsSet().getSubject();
            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            OffsetDateTime issuedAt = OffsetDateTime.ofInstant(signedJWT.getJWTClaimsSet().getIssueTime().toInstant(), ZoneOffset.UTC);
            OffsetDateTime expiresAt = OffsetDateTime.ofInstant(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(), ZoneOffset.UTC);

            // 추출된 토큰 메타데이터를 JwtTokenEntity 객체로 변환
            JwtTokenEntity entity = new JwtTokenEntity(jti, username, tokenType, issuedAt, expiresAt);

            log.info("[TokenProvider] toEntity 결과: jti={}, username={}, type={}", jti, username, tokenType);

            return entity;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
}


