package com.sprint.mission.discodeit.security.jwt.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_jwt_token")
@Getter
@Setter
@NoArgsConstructor
public class JwtTokenEntity {

  @Id
  @Column(name = "jti", length = 64)
  private String jti;

  // 사용자명(주체)
  @Column(name = "username", nullable = false)
  private String username;

  // 토큰 타입(access | refresh)
  @Column(name = "token_type", nullable = false, length = 16)
  private String tokenType; // access | refresh

  // 발급 시각(UTC)
  @Column(name = "issued_at", nullable = false)
  private OffsetDateTime issuedAt;

  // 만료 시각(UTC)
  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  // 폐기 여부
  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  // 회전 시 새 리프레시 토큰의 jti
  @Column(name = "replaced_by", length = 64)
  private String replacedBy;

  public JwtTokenEntity(String jti, String username, String tokenType, OffsetDateTime issuedAt,
      OffsetDateTime expiresAt) {
    this.jti = jti;
    this.username = username;
    this.tokenType = tokenType;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }
}
