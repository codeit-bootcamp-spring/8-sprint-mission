package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.store.JwtTokenEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  // HTTP 쿠키의 이름
  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH-TOKEN";

  // 만료 시간(밀리초 단위)
  private final int accessTokenExpirationMs;
  private final int refreshTokenExpirationMs;

  // 서명자
  private final JWSSigner accessTokenSigner;
  private final JWSSigner refreshTokenSigner;

  // 검증자
  private final JWSVerifier accessTokenVerifier;
  private final JWSVerifier refreshTokenVerifier;


  public JwtTokenProvider(@Value("${jwt.access-token.secret}") String accessTokenSecret,
      @Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
      @Value("${jwt.access-token.exp}") int accessTokenExpirationMs,
      @Value("${jwt.refresh-token.exp}") int refreshTokenExpirationMs) throws JOSEException {

    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;

    // Access
    byte[] accessSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenSigner = new MACSigner(accessSecretBytes);
    this.accessTokenVerifier = new MACVerifier(accessSecretBytes);

    // Refresh
    byte[] refreshSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.refreshTokenSigner = new MACSigner(refreshSecretBytes);
    this.refreshTokenVerifier = new MACVerifier(refreshSecretBytes);
  }

  // Access 생성
  public String generateAccessToken(DiscodeitUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, "access");
  }

  // Refresh 생성
  public String generateRefreshToken(DiscodeitUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, "refresh");
  }

  // 토큰 생성 공통 로직
  private String generateToken(DiscodeitUserDetails userDetails, int expirationMs, JWSSigner signer,
      String tokenType) throws JOSEException {
    String tokenId = UUID.randomUUID().toString();
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(userDetails.getUsername())
        .jwtID(tokenId).claim("userName", userDetails.getUsername()).claim("type", tokenType)
        .claim("roles",
            userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
        .issueTime(now).expirationTime(expiryDate).build();

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

    signedJWT.sign(signer);

    return signedJWT.serialize();
  }

  // Cookie에 Refresh 저장
  public Cookie generateRefreshTokenCookie(String refreshToken) {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(refreshTokenExpirationMs / 1000);

    return cookie;
  }

  // Cookie에 Refresh 즉시 제거
  public Cookie generateRefreshTokenExpirationCookie() {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");

    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    return cookie;
  }

  // Response에 Refresh 쿠키 추가
  public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = generateRefreshTokenCookie(refreshToken);
    response.addCookie(cookie);
  }

  // Response에 Refresh 쿠키 만료
  public void expireRefreshCookie(HttpServletResponse response) {
    Cookie cookie = generateRefreshTokenExpirationCookie();
    response.addCookie(cookie);
  }

  // Access 토큰 검증
  public boolean validateAccessToken(String token) {
    return verifyToken(token, accessTokenVerifier, "access");
  }

  // Refresh 토큰 검증
  public boolean validateRefreshToken(String token) {
    return verifyToken(token, refreshTokenVerifier, "refresh");
  }

  // 토큰 검증 공통 로직
  private boolean verifyToken(String token, JWSVerifier verifier, String expectedType) {

    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 서명 검증
      if (!signedJWT.verify(verifier)) {
        return false;
      }

      // 유형 검증
      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!expectedType.equals(tokenType)) {
        return false;
      }

      // 만료 시간 검증
      Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

      // 모두 통과했으면 유효한 토큰으로 간주한다.
      return exp != null && exp.after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  // 사용자명 추출
  public String getUsernameFromToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  // 토큰에서 고유 식별자(jti) 추출
  public String getTokenId(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getJWTID();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  // 발급 시각(iat) 추출
  public Date getIssuedAt(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getIssueTime();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  // 만료 시간(exp) 추출
  public Date getExpiration(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getExpirationTime();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  // 토큰에서 메타데이터 추출하여 JwtTokenEntity로 변환
  public JwtTokenEntity toEntity(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 토큰 클레임 추출
      String jti = signedJWT.getJWTClaimsSet().getJWTID();
      String username = signedJWT.getJWTClaimsSet().getSubject();
      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      OffsetDateTime issuedAt = OffsetDateTime.ofInstant(
          signedJWT.getJWTClaimsSet().getIssueTime().toInstant(), ZoneOffset.UTC);
      OffsetDateTime expiresAt = OffsetDateTime.ofInstant(
          signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(), ZoneOffset.UTC);
      
      return new JwtTokenEntity(jti, username, tokenType, issuedAt, expiresAt);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }
}
