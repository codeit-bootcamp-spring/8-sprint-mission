package com.sprint.mission.discodeit.DTO.response;

import com.sprint.mission.discodeit.entity.ChannelStatus;
import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        UUID uid,
        ChannelStatus status,
        Optional<String> name,
        String host,
        Optional<String> description,
        int participant,
        List<String> participants,
        Instant latestMessageAt,
        List<UUID> participantUserIds,
        Instant createAt,
        Instant modifiedAt
) {
    public static ChannelResponse ofPublic(
            UUID id,
            UUID uid,
            String name,
            String host,
            String description,
            int participant,
            List<String> participants,
            Instant latestMessageAt,
            Instant createdAt,
            Instant modifiedAt
    ) {
        return new ChannelResponse(id, uid, ChannelStatus.PUBLIC, Optional.of(name), host, Optional.of(description), participant, participants, latestMessageAt, List.of(), createdAt, modifiedAt);
    }

    public static ChannelResponse ofPrivate(
            UUID id,
            UUID uid,
            String host,
            int participant,
            List<String> participants,
            Instant latestMessageAt,
            List<UUID> participantUserIds,
            Instant createAt,
            Instant modifiedAt
    ) {
        return new ChannelResponse(id, uid, ChannelStatus.PRIVATE, Optional.empty(), host, Optional.empty(), participant, participants, latestMessageAt, participantUserIds, createAt, modifiedAt);
    }

    public static ChannelResponse from(Channel channel, Instant latestMessageAt, List<UUID> participantUserIds) {
        return switch(channel.getStatus()) {
            case PUBLIC -> ofPublic(
                    channel.getId(),
                    channel.getUid(),
                    channel.getName(),
                    channel.getHost(),
                    channel.getDescription(),
                    channel.getParticipant(),
                    channel.getParticipants(),
                    latestMessageAt,
                    channel.getCreatedAt(),
                    channel.getModifiedAt()
            );
            case PRIVATE -> ofPrivate(
                    channel.getId(),
                    channel.getUid(),
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
