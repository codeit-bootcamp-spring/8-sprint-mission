package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH-TOKEN";

  // 액세스 토큰의 만료 시간(밀리초 단위)
  private final int accessTokenExpirationMs;
  // 리프레시 토큰의 만료 시간(밀리초 단위)
  private final int refreshTokenExpirationMs;

  // 액세스 토큰을 서명하기 위한 서명자
  private final JWSSigner accessTokenSigner;
  // 액세스 토큰의 서명을 검증하기 위한 검증자
  private final JWSVerifier accessTokenVerifier;
  // 리프레시 토큰을 서명하기 위한 서명자
  private final JWSSigner refreshTokenSigner;
  // 리프레시 토큰의 서명을 검증하기 위한 검증자
  private final JWSVerifier refreshTokenVerifier;

  public JwtTokenProvider(
      @Value("${jwt.access-token.secret}") String accessTokenSecret,
      @Value("${jwt.access-token.exp}") int accessTokenExpirationMs,
      @Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
      @Value("${jwt.refresh-token.exp}") int refreshTokenExpirationMs
  ) throws JOSEException {

    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;

    byte[] accessSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenSigner = new MACSigner(accessSecretBytes);
    this.accessTokenVerifier = new MACVerifier(accessSecretBytes);

    byte[] refreshSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.refreshTokenSigner = new MACSigner(refreshSecretBytes);
    this.refreshTokenVerifier = new MACVerifier(refreshSecretBytes);
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, "access");
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, "refresh");
  }

  private String generateToken(
      DiscodeitUserDetails userDetails,
      int expirationMs,
      JWSSigner signer,
      String tokenType
  )
      throws JOSEException {
    String tokenId = UUID.randomUUID().toString();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(userDetails.getUsername())
        .jwtID(tokenId)
        .claim("type", tokenType)
        .claim("roles",
            userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
        .issueTime(now)
        .expirationTime(expiryDate)
        .build();

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
    signedJWT.sign(signer);
    return signedJWT.serialize();
  }

  public Cookie generateRefreshTokenCookie(String refreshToken) {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);  // 로컬 테스트는 false (운영은 true)
    cookie.setPath("/");
    cookie.setMaxAge(refreshTokenExpirationMs / 1000);
    return cookie;
  }

  public boolean validateAccessToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.verify(accessTokenVerifier) &&
          signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());
    } catch (Exception e) {
      log.error("Invalid Access Token: {}", e.getMessage());
      return false;
    }
  }

  private boolean validateToken(String token, JWSVerifier verifier, String expectedType) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 서명 검증
      if (!signedJWT.verify(verifier)) {
        log.debug("{} 토큰 서명 검증 실패", expectedType);
        return false;
      }

      // 토큰 타입 체크 (access인지 refresh인지)
      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!expectedType.equals(tokenType)) {
        log.debug("토큰 타입 불일치: 기대값={}, 실제값={}", expectedType, tokenType);
        return false;
      }

      // 만료 시간 체크
      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expirationTime == null || expirationTime.before(new Date())) {
        log.debug("{} 토큰 만료됨", expectedType);
        return false;
      }

      return true;
    } catch (Exception e) {
      log.debug("{} 토큰 검증 중 예외 발생: {}", expectedType, e.getMessage());
      return false;
    }
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token, refreshTokenVerifier, "refresh");
  }

  public String getUsernameFromToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (Exception e) {
      return null;
    }
  }
}
