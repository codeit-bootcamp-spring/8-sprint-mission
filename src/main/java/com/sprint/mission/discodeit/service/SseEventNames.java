package com.sprint.mission.discodeit.service;

/**
 * 클라이언트 EventSource 구독 이름과 일치하는 SSE 이벤트 이름.
 */
public final class SseEventNames {

	private SseEventNames() {
	}

	public static final String NOTIFICATIONS_CREATED = "notifications.created";
	public static final String BINARY_CONTENTS_UPDATED = "binaryContents.updated";
	public static final String CHANNELS_CREATED = "channels.created";
	public static final String CHANNELS_UPDATED = "channels.updated";
	public static final String CHANNELS_DELETED = "channels.deleted";
	public static final String USERS_CREATED = "users.created";
	public static final String USERS_UPDATED = "users.updated";
	public static final String USERS_DELETED = "users.deleted";
}
