package com.sprint.mission.discodeit.security.jwt.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/* JWT 토큰 메타데이터를 저장/조회하는 JPA 레포지토리 */
public interface JwtTokenRepository extends JpaRepository<JwtTokenEntity, String> {

    // 특정 사용자명의 모든 토큰을 조회한다.
    List<JwtTokenEntity> findByUsername(String username);
}
