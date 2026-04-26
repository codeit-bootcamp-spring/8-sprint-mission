package com.sprint.mission.discodeit.cache;

/**
 * {@link org.springframework.cache.annotation.Cacheable} 등에서 사용하는 캐시 이름.
 */
public final class CacheNames {

  public static final String CHANNELS_BY_USER = "channelsByUser";
  public static final String NOTIFICATIONS_BY_USER = "notificationsByUser";
  public static final String USERS_ALL = "usersAll";

  private CacheNames() {
  }
}
