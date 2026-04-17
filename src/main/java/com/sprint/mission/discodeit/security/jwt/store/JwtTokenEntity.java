package com.sprint.mission.discodeit.security.jwt.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

/**
 * JWT 토큰 메타데이터를 저장하는 엔티티.
 * - jti: 토큰 고유 식별자
 * - username: 토큰 소유자(주체)
 * - tokenType: access | refresh
 * - issuedAt/expiresAt: 발급/만료 시각(UTC)
 * - revoked: 폐기 여부
 * - replacedBy: 리프레시 회전 시 새 RT의 jti
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "tbl_jwt_token")
public class JwtTokenEntity {

    // 토큰 고유 식별자(jti)
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

    /**
     * 필수 메타데이터로 엔티티를 구성하는 생성자.
     */
    public JwtTokenEntity(String jti, String username, String tokenType, OffsetDateTime issuedAt, OffsetDateTime expiresAt) {
        this.jti = jti;
        this.username = username;
        this.tokenType = tokenType;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
