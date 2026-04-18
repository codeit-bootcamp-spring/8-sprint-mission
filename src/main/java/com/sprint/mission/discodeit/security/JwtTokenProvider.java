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
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH-TOKEN";

  private final int accessTokenExpirationMs;
  private final int refreshTokenExpirationMs;

  private final JWSSigner accessTokenSigner;
  private final JWSSigner refreshTokenSigner;

  private final JWSVerifier accessTokenVerifier;
  private final JWSVerifier refreshTokenVerifier;


  public JwtTokenProvider(JwtProperties jwtProperties) throws JOSEException {

    this.accessTokenExpirationMs = jwtProperties.accessToken().exp();
    this.refreshTokenExpirationMs = jwtProperties.refreshToken().exp();

    byte[] accessSecretBytes = jwtProperties.accessToken().secret()
        .getBytes(StandardCharsets.UTF_8);

    // 검증
    validateSecretKey(accessSecretBytes, "Access secret key");
    
    this.accessTokenSigner = new MACSigner(accessSecretBytes);
    this.accessTokenVerifier = new MACVerifier(accessSecretBytes);

    byte[] refreshSecretBytes = jwtProperties.refreshToken().secret()
        .getBytes(StandardCharsets.UTF_8);

    // 검증
    validateSecretKey(refreshSecretBytes, "Refresh secret key");

    this.refreshTokenSigner = new MACSigner(refreshSecretBytes);
    this.refreshTokenVerifier = new MACVerifier(refreshSecretBytes);
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, "access");
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, "refresh");
  }

  private void validateSecretKey(byte[] secretBytes, String keyName) {
    if (secretBytes.length < 32) {
      throw new IllegalArgumentException(
          String.format("%s는 최소 32바이트 이상이어야 합니다. (현재: %d바이트)", keyName,
              secretBytes.length)
      );
    }
  }

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

  public ResponseCookie generateRefreshTokenCookie(String refreshToken) {

    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken).path("/").httpOnly(true)
        .secure(false).sameSite("lax").maxAge(refreshTokenExpirationMs).build();
  }

  public Cookie generateRefreshTokenExpirationCookie() {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");

    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    return cookie;
  }

  public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie = generateRefreshTokenCookie(refreshToken);
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public void expireRefreshCookie(HttpServletResponse response) {
    Cookie cookie = generateRefreshTokenExpirationCookie();
    response.addCookie(cookie);
  }

  public boolean validateAccessToken(String token) {
    return verifyToken(token, accessTokenVerifier, "access");
  }

  public boolean validateRefreshToken(String token) {
    return verifyToken(token, refreshTokenVerifier, "refresh");
  }

  private boolean verifyToken(String token, JWSVerifier verifier, String expectedType) {

    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(verifier)) {
        return false;
      }

      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!expectedType.equals(tokenType)) {
        return false;
      }

      Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

      return exp != null && exp.after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public String getTokenId(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getJWTID();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public Date getIssuedAt(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getIssueTime();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public Date getExpiration(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      return signedJWT.getJWTClaimsSet().getExpirationTime();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public JwtTokenEntity toEntity(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

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
