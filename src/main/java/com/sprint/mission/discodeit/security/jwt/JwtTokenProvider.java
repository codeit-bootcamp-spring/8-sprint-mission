package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.enums.AuthErrorCode;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
  private static final String ACCESS_TOKEN_CLAIM_KEY = "userId";
  // HMAC 서명 알고리즘
  private static final JWSAlgorithm algorithm = JWSAlgorithm.HS256;

  private final JwtProperties jwtProperties;

  // 액세스 토큰 발급
  public String generateAccessToken(UUID userId, String username) {

    Map<String, Object> extraClaims = Map.of(ACCESS_TOKEN_CLAIM_KEY, userId.toString());

    try {
      return generateToken(username, jwtProperties.accessTokenExpiry(), extraClaims);
    } catch (JOSEException e) {
      throw new DiscodeitException(AuthErrorCode.AUTHENTICATION_FAILED, e.getMessage());
    }
  }

  // 리프레시 토큰 발급
  public String generateRefreshToken(UUID userId) {

    try {
      return generateToken(userId.toString(), jwtProperties.refreshTokenExpiry(), null);
    } catch (JOSEException e) {
      throw new DiscodeitException(AuthErrorCode.AUTHENTICATION_FAILED, e.getMessage());
    }
  }

  // 토큰에서 username 추출 (AccessToken)
  public String extractUsername(String token) {

    try {
      return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
    } catch (Exception e) {
      throw new DiscodeitException(AuthErrorCode.INVALID_TOKEN, e.getMessage());
    }
  }

  // 토큰에서 userId 추출 (RefreshToken)
  public UUID extractUserId(String token) {

    try {
      String subject = SignedJWT.parse(token).getJWTClaimsSet().getSubject();
      return UUID.fromString(subject);
    } catch (Exception e) {
      throw new DiscodeitException(AuthErrorCode.INVALID_TOKEN, e.getMessage());
    }
  }

  // 토큰 만료 시각 추출
  public Date extractExpiration(String token) {

    try {
      return SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime();
    } catch (Exception e) {
      throw new DiscodeitException(AuthErrorCode.INVALID_TOKEN, e.getMessage());
    }
  }

  // RefreshToken을 HttpOnly 쿠키로 만듬
  public ResponseCookie buildRefreshTokenCookie(String refreshToken) {

    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(jwtProperties.refreshTokenExpiry() / 1000)
        .build();
  }

  // 빈 RefreshToken 쿠키 -> 삭제용도
  public ResponseCookie buildExpiredRefreshTokenCookie() {

    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(0)
        .build();
  }

  // 토큰 유효성 검증
  public boolean validateToken(String token) {

    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(jwtProperties.secret().getBytes());
      return signedJWT.verify(verifier) &&
          signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  // 토큰 발급 공통 로직 헬퍼 메서드
  private String generateToken(String subject, long expiryMillis, Map<String, Object> extraClaims)
      throws JOSEException {

    Instant now = Instant.now();
    JWSSigner signer = new MACSigner(jwtProperties.secret().getBytes());

    JWTClaimsSet.Builder builder = new Builder()
        .subject(subject)
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusMillis(expiryMillis)));

    // 추가 클레임이 들어오면 Builder에 넣어줌
    if (extraClaims != null && !extraClaims.isEmpty()) {
      extraClaims.forEach(builder::claim);
    }

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(algorithm), builder.build());
    signedJWT.sign(signer);

    return signedJWT.serialize();
  }
}
