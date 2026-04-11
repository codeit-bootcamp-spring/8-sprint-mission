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
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.exception.user.InvalidTokenException;
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

  private static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final int accessTokenExpirationMs;
  private final int refreshTokenExpirationMs;

  private final JWSSigner accessTokenSigner;
  private final JWSVerifier accessTokenVerifier;
  private final JWSSigner refreshTokenSigner;
  private final JWSVerifier refreshTokenVerifier;

  public JwtTokenProvider(
      @Value("${jwt.access-token.secret}") String accessTokenSecret,
      @Value("${jwt.access-token.exp}") int accessTokenExpirationMs,
      @Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
      @Value("${jwt.refresh-token.exp}") int refreshTokenExpirationMs
  ) throws JOSEException {
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;

    byte[] accessTokenSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenSigner = new MACSigner(accessTokenSecretBytes);
    this.accessTokenVerifier = new MACVerifier(accessTokenSecretBytes);

    byte[] refreshTokenSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.refreshTokenSigner = new MACSigner(refreshTokenSecretBytes);
    this.refreshTokenVerifier = new MACVerifier(refreshTokenSecretBytes);
  }

  // Access Token 생성
  public String generateAccessToken(DiscodeitUserDetails userDetails) throws JOSEException {

    return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, "access");
  }

  // Refresh Token 생성
  public String generateRefreshToken(DiscodeitUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, "refresh");
  }

  // Token 생성
  private String generateToken(DiscodeitUserDetails userDetails, int expirationMs, JWSSigner signer,
      String tokenType) throws JOSEException {

    String tokenId = UUID.randomUUID().toString();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(userDetails.getUsername())
        .jwtID(tokenId)
        .claim("userId", userDetails.getUserDto().id())
        .claim("type", tokenType)
        .claim("roles",
            userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
        )
        .issueTime(now)
        .expirationTime(expiryDate)
        .build();

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

    signedJWT.sign(signer);

    String completedJWT = signedJWT.serialize();

    return completedJWT;
  }

  // Refresh Token을 HttpOnly가 적용된 쿠키로 생성
  public Cookie generateRefreshTokenCookie(String refreshToken) {

    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(refreshTokenExpirationMs / 1000);

    return cookie;
  }

  // Refresh Token 쿠키를 만료시키는 쿠기 생성
  public Cookie generateRefreshTokenExpirationCookie() {

    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");

    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    return cookie;
  }

  // Refresh Token은 담은 HttpOnly가 적용된 쿠키를 응답에 추가
  public void addRefreshCookie(HttpServletResponse response, String refreshToken) {

    Cookie cookie = generateRefreshTokenCookie(refreshToken);

    response.addCookie(cookie);
  }

  // 만료된 Refresh Token 쿠키를 응답에 추가
  public void expireRefreshCookie(HttpServletResponse response) {

    Cookie cookie = generateRefreshTokenExpirationCookie();

    response.addCookie(cookie);
  }

  // Access Token 검증
  public boolean validateAccessToken(String token) {

    boolean result = verifyToken(token, accessTokenVerifier, "access");

    return result;
  }

  // Refresh Token 검증
  public boolean validateRefreshToken(String token) {

    boolean result = verifyToken(token, refreshTokenVerifier, "refresh");

    return result;
  }

  // Token 검증
  private boolean verifyToken(String token, JWSVerifier verifier, String expectedType) {

    try {
      // 토큰 파싱
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 서명 무결성 검증
      if (!signedJWT.verify(verifier)) {
        return false;
      }

      // 토큰 타입 검증
      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!expectedType.equals(tokenType)) {
        return false;
      }

      // 만료 시간 검증
      Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
      boolean valid = exp != null && exp.after(new Date());

      return valid;
    } catch (Exception e) {

      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      String subject = signedJWT.getJWTClaimsSet().getSubject();

      return subject;
    } catch (Exception e) {
      throw new InvalidTokenException();
    }
  }

  public String getTokenId(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      String jti = signedJWT.getJWTClaimsSet().getJWTID();

      return jti;
    } catch (Exception e) {
      throw new InvalidTokenException();
    }
  }

  public Date getIssuedAt(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      Date iat = signedJWT.getJWTClaimsSet().getIssueTime();

      return iat;
    } catch (Exception e) {
      throw new InvalidTokenException();
    }
  }

  public Date getExpiration(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

      return exp;
    } catch (Exception e) {
      throw new InvalidTokenException();
    }
  }
}
