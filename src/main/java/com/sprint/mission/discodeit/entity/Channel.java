package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final UUID userId;
    private final Instant createdAt;
    private Instant modifiedAt;
    private final ChannelStatus status;
    private String name;
    private final String host;
    private String description;
    private Integer participant;
    private List<String> participants;

    public Channel(UUID userId, ChannelStatus status, String name, String host, String description, Integer participant, List<String> participants) {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        modifiedAt = createdAt;
        this.userId = userId;
        this.status = status;
        this.name = name;
        this.host = host;
        this.description = description;
        this.participant = participant;
        this.participants = participants;
    }

    public String getFileName() {
        return id.toString().concat(".ser");
    }

    public void update(String name, String description) {
        boolean anyValueUpdated = false;
        if(name != null && !name.equals(this.name)) {
            this.name = name;
            anyValueUpdated = true;
        }
        if(description != null && !description.equals(this.description)) {
            this.description = description;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.modifiedAt = Instant.now();
        }
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", uid=" + userId +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", description='" + description + '\'' +
                ", participant=" + participant +
                ", participants=" + participants +
                '}';
    }

}
