package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        UUID userId,
        ChannelStatus status,
        Optional<String> channelName,
        String host,
        Optional<String> description,
        Optional<Integer> participant,
        Optional<List<String>> participants,
        Instant latestMessageAt,
        List<UUID> participantUserIds,
        Instant createAt,
        Instant modifiedAt
) {
    public static ChannelResponse ofPublic(
            UUID id,
            UUID userId,
            String name,
            String host,
            String description,
            Integer participant,
            List<String> participants,
            Instant latestMessageAt,
            Instant createdAt,
            Instant modifiedAt
    ) {
        return new ChannelResponse(id, userId, ChannelStatus.PUBLIC, Optional.of(name), host, Optional.of(description), Optional.empty(), Optional.empty(), latestMessageAt, List.of(), createdAt, modifiedAt);
    }

    public static ChannelResponse ofPrivate(
            UUID id,
            UUID userId,
            String host,
            int participant,
            List<String> participants,
            Instant latestMessageAt,
            List<UUID> participantUserIds,
            Instant createAt,
            Instant modifiedAt
    ) {
        return new ChannelResponse(id, userId, ChannelStatus.PRIVATE, Optional.empty(), host, Optional.empty(), Optional.of(participant), Optional.of(participants), latestMessageAt, participantUserIds, createAt, modifiedAt);
    }

    public static ChannelResponse from(Channel channel, Instant latestMessageAt, List<UUID> participantUserIds) {
        return switch(channel.getStatus()) {
            case PUBLIC -> ofPublic(
                    channel.getId(),
                    channel.getUserId(),
                    channel.getName(),
                    channel.getHost(),
                    channel.getDescription(),
                    null,
                    null,
                    latestMessageAt,
                    channel.getCreatedAt(),
                    channel.getModifiedAt()
            );
            case PRIVATE -> ofPrivate(
                    channel.getId(),
                    channel.getUserId(),
                    channel.getHost(),
                    channel.getParticipant(),
                    channel.getParticipants(),
                    latestMessageAt,
                    participantUserIds,
                    channel.getCreatedAt(),
                    channel.getModifiedAt()
            );
        };
    }
}
