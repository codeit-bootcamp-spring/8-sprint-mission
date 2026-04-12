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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 토큰 발급, 갱신, 검증 담당하는 클래스 토큰과 관련된 모든 로직을 모아놓아서 다른 곳에서 내부를 알지 않아도 된다. (캡슐화)
 */
@Slf4j
@Component
public class JwtTokenProvider {

  private final String secretKey;
  private final long accessTokenValidity;
  private final long refreshTokenValidity;

  public JwtTokenProvider(@Value("${discodeit.security.jwt.secret}") String secretKey,
      @Value("${discodeit.security.jwt.access-token-validity}") long accessTokenValidity,
      @Value("${discodeit.security.jwt.refresh-token-validity}") long refreshTokenValidity) {
    this.secretKey = secretKey;
    this.accessTokenValidity = accessTokenValidity;
    this.refreshTokenValidity = refreshTokenValidity;
  }

  // 액세스 토큰 생성
  public String createAccessToken(UUID userId, String role) {
    return createToken(userId, role, accessTokenValidity);
  }

  // 리프레시 토큰 생성
  public String createRefreshToken(UUID userId, String role) {
    return createToken(userId, role, refreshTokenValidity);
  }

  // 토큰 만료 시간 추출
  public Instant getExpirationTime(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

      if (expirationTime == null) {
        throw new RuntimeException("토큰에 만료 시간 정보가 포함되어 있지 않습니다.");
      }

      return expirationTime.toInstant();

    } catch (Exception e) {
      log.error("토큰에서 만료 시간을 추출하는데 실패했습니다. token: {}, error: {}", token, e.getMessage());
      throw new RuntimeException("유효하지 않은 토큰 형식입니다.", e);
    }
  }

  // 토큰 생성 공통 메서드
  public String createToken(UUID userId, String role, long validitySeconds) {

    try {
      // 페이로드 만들기: 토큰에 담을 정보 조각들(Claims)을 정의
      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
          .subject(userId.toString())                                                // 'sub' 클레임: 토큰의 주인 (유저 ID)
          .claim("role", role)                                                // 커스텀 클레임: 유저의 권한 목록 포함
          .issueTime(new Date())                                                     // 'iat' 클레임: 토큰 발행 시각
          .expirationTime(new Date(System.currentTimeMillis() + validitySeconds))  // 'exp' 클레임: 만료 시각 지정
          .build();

      // Header(토큰의 메타데이터) 만들기: 이 토큰을 어떤 알고리즘으로 암호화 했는지
      JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

      // 서명자(Signer): 우리 서버만 아는 '비밀키'를 펜처럼 쥐고 있는 서명자
      JWSSigner signer = new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8));

      // 토큰 조립 및 서명: 헤더와 페이로드 결합
      // signedJWT라는 서류에 미리 만들어둔 헤더와 페이로드를 붙여주는 것
      SignedJWT signedJWT = new SignedJWT(header, claimsSet);

      // 서명자가 비밀키라는 펜으로 서류에 서명 하는 것
      // Signature 파트가 생성된다.
      signedJWT.sign(signer);

      // 직렬화: 객체를 전송하기 쉬운 '한 줄의 문자열'로 변환하여 반환
      // 결과값(예시): eyaDbGci... (헤더).eyaVbGci... (페이로드).SflKxwR... (서명)
      return signedJWT.serialize();

    } catch (JOSEException e) {
      throw new RuntimeException("토큰 생성 실패", e);
    }
  }

  // 정보 추출
  public String extractSubject(String token) {
    try {
      // 파싱 후 페이로드 영역의 'subject' 값을 문자열로 반환한다.
      return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new RuntimeException("토큰에서 사용자 정보를 꺼내는 데 실패했습니다.", e);
    }
  }

  // 토큰 유효성 검사
  // 1) 우리 서버의 비밀키로 서명한게 맞는지
  // 2) 아직 쓸 수 있는지(만료) 확인한다.
  public boolean validateToken(String token) {
    try {

      // 문자열로 되어있는 토큰을 다시 객체(서류) 형태로 파싱한다.
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 검증 도구 준비
      // '비밀키'라는 검증기를 가지고 있는 검증원
      JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));

      // 서명 검증
      // 데이터가 한글자라도 수정되었으면 여기서 false가 나온다.
      // "어? 내 검증기 안에 내장되어 있는 비밀키랑 좀 다른데?" 나오면 실패한 것
      if (!signedJWT.verify(verifier)) {
        log.warn("위조 감지: 서명이 일치하지 않는 토큰입니다.");
        return false;
      }

      // 시간 검증 (만료 시간)
      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

      if (expirationTime.before(new Date())) {
        log.warn("만료된 토큰입니다.");
        return false;
      }

      // 모든 관문 통과하면 이전에 작성했던 진짜 서류임을 인정한다.
      return true;

    } catch (Exception e) {
      log.error("JWT 검증 실패: {}", e.getMessage());
      return false;
    }
  }
}
