package com.sprint.mission.discodeit.cache;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("cacheSpelKeys")
public class CacheSpelKeys {

  public UUID userIdFrom(Authentication authentication) {
    if (authentication == null
        || !(authentication.getPrincipal() instanceof DiscodeitUserDetails details)) {
      throw new com.sprint.mission.discodeit.exception.UnauthenticatedException("Unauthenticated");
    }
    return details.getUserDto().id();
  }
}
