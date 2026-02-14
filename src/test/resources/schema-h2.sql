DROP SCHEMA IF EXISTS discodeit_schema CASCADE;

CREATE SCHEMA IF NOT EXISTS discodeit_schema;

SET SCHEMA discodeit_schema;

CREATE TABLE binary_contents
(
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL
);

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    username   VARCHAR(50)              NOT NULL UNIQUE,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    password   VARCHAR(60)              NOT NULL,
    profile_id UUID UNIQUE,
    CONSTRAINT fk_users_profile FOREIGN KEY (profile_id)
        REFERENCES binary_contents (id) ON DELETE SET NULL
);

CREATE TABLE channels
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE,
    name        VARCHAR(100),
    description VARCHAR(500),
    type        VARCHAR(10)              NOT NULL,
    CONSTRAINT chk_channels_type CHECK (type IN ('PUBLIC', 'PRIVATE'))
);

CREATE TABLE user_statuses
(
    id             UUID PRIMARY KEY,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE,
    user_id        UUID                     NOT NULL UNIQUE,
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_user_statuses_users FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE read_statuses
(
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE,
    user_id      UUID                     NOT NULL,
    channel_id   UUID                     NOT NULL,
    last_read_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_read_statuses_user_channel UNIQUE (user_id, channel_id),
    CONSTRAINT fk_read_statuses_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_read_statuses_channel FOREIGN KEY (channel_id)
        REFERENCES channels (id) ON DELETE CASCADE
);

CREATE TABLE messages
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    content    CLOB,
    channel_id UUID                     NOT NULL,
    author_id  UUID,
    CONSTRAINT fk_messages_channel FOREIGN KEY (channel_id)
        REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_author FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE message_attachments
(
    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,
    PRIMARY KEY (message_id, attachment_id),
    CONSTRAINT fk_attachments_message FOREIGN KEY (message_id)
        REFERENCES messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_attachments_binary FOREIGN KEY (attachment_id)
        REFERENCES binary_contents (id) ON DELETE CASCADE
);