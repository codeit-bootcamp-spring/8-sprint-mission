package com.sprint.mission.discodeit.security.store;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtSessionRegistry {

  private final JwtTokenRepository jwtTokenRepository;

  public void register(JwtTokenEntity token) {
    jwtTokenRepository.save(token);
  }

  public void revokeAllByUsername(String username) {
    List<JwtTokenEntity> tokens = jwtTokenRepository.findByUsername(username);
    for (JwtTokenEntity t : tokens) {
      t.setRevoked(true);
    }

    jwtTokenRepository.saveAll(tokens);
  }

  @Transactional(readOnly = true)
  public boolean isRevoked(String jti) {
    return jwtTokenRepository.findById(jti).map(JwtTokenEntity::isRevoked).orElse(false);
  }

  public void markReplaced(String oldJti, String newJti) {

    jwtTokenRepository.findById(oldJti).ifPresent(t -> {
      t.setRevoked(true);
      t.setReplacedBy(newJti);
      jwtTokenRepository.save(t);
    });
  }

  public void revokeByJti(String jti) {
    jwtTokenRepository.findById(jti).ifPresent(t -> {
      t.setRevoked(true);
      jwtTokenRepository.save(t);
    });
  }
}
