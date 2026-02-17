CREATE TABLE binary_contents
(
    id           UUID                     NOT NULL,
    created_at   TIMESTAMP with time zone NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL,
    CONSTRAINT pk_binary_contents PRIMARY KEY (id)
);

CREATE TABLE channels
(
    id          UUID                     NOT NULL,
    updated_at  TIMESTAMP with time zone,
    created_at  TIMESTAMP with time zone NOT NULL,
    type        VARCHAR(255)             NOT NULL,
    name        VARCHAR(100),
    description VARCHAR(500),
    CONSTRAINT pk_channels PRIMARY KEY (id)
);

CREATE TABLE message_attachments
(
    attachment_id UUID NOT NULL,
    message_id    UUID NOT NULL
);

CREATE TABLE messages
(
    id         UUID                     NOT NULL,
    updated_at TIMESTAMP with time zone,
    created_at TIMESTAMP with time zone NOT NULL,
    content    TEXT                     NOT NULL,
    channel_id UUID                     NOT NULL,
    author_id  UUID,
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

CREATE TABLE read_statuses
(
    id           UUID                     NOT NULL,
    updated_at   TIMESTAMP with time zone,
    created_at   TIMESTAMP with time zone NOT NULL,
    user_id      UUID                     NOT NULL,
    channel_id   UUID                     NOT NULL,
    last_read_at TIMESTAMP with time zone NOT NULL,
    CONSTRAINT pk_read_statuses PRIMARY KEY (id)
);

CREATE TABLE user_statuses
(
    id             UUID                     NOT NULL,
    updated_at     TIMESTAMP with time zone,
    created_at     TIMESTAMP with time zone NOT NULL,
    user_id        UUID                     NOT NULL,
    last_active_at TIMESTAMP with time zone NOT NULL,
    CONSTRAINT pk_user_statuses PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID                     NOT NULL,
    updated_at TIMESTAMP with time zone,
    created_at TIMESTAMP with time zone NOT NULL,
    username   VARCHAR(50)              NOT NULL,
    email      VARCHAR(100)             NOT NULL,
    password   VARCHAR(60)              NOT NULL,
    profile_id UUID,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE read_statuses
    ADD CONSTRAINT uc_3fd6e4741dcb586be6de40913 UNIQUE (user_id, channel_id);

ALTER TABLE message_attachments
    ADD CONSTRAINT uc_message_attachments_attachment UNIQUE (attachment_id);

ALTER TABLE user_statuses
    ADD CONSTRAINT uc_user_statuses_user UNIQUE (user_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_profile UNIQUE (profile_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE read_statuses
    ADD CONSTRAINT FK_READ_STATUSES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE read_statuses
    ADD CONSTRAINT FK_READ_STATUSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_PROFILE FOREIGN KEY (profile_id) REFERENCES binary_contents (id);

ALTER TABLE user_statuses
    ADD CONSTRAINT FK_USER_STATUSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE message_attachments
    ADD CONSTRAINT fk_mesatt_on_binary_content FOREIGN KEY (attachment_id) REFERENCES binary_contents (id);

ALTER TABLE message_attachments
    ADD CONSTRAINT fk_mesatt_on_message FOREIGN KEY (message_id) REFERENCES messages (id);
CREATE TABLE binary_contents
(
    id           UUID                     NOT NULL,
    created_at   TIMESTAMP with time zone NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL,
    CONSTRAINT pk_binary_contents PRIMARY KEY (id)
);

CREATE TABLE channels
(
    id          UUID                     NOT NULL,
    updated_at  TIMESTAMP with time zone,
    created_at  TIMESTAMP with time zone NOT NULL,
    type        VARCHAR(255)             NOT NULL,
    name        VARCHAR(100),
    description VARCHAR(500),
    CONSTRAINT pk_channels PRIMARY KEY (id)
);

CREATE TABLE message_attachments
(
    attachment_id UUID NOT NULL,
    message_id    UUID NOT NULL
);

CREATE TABLE messages
(
    id         UUID                     NOT NULL,
    updated_at TIMESTAMP with time zone,
    created_at TIMESTAMP with time zone NOT NULL,
    content    TEXT                     NOT NULL,
    channel_id UUID                     NOT NULL,
    author_id  UUID,
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

CREATE TABLE read_statuses
(
    id           UUID                     NOT NULL,
    updated_at   TIMESTAMP with time zone,
    created_at   TIMESTAMP with time zone NOT NULL,
    user_id      UUID                     NOT NULL,
    channel_id   UUID                     NOT NULL,
    last_read_at TIMESTAMP with time zone NOT NULL,
    CONSTRAINT pk_read_statuses PRIMARY KEY (id)
);

CREATE TABLE user_statuses
(
    id             UUID                     NOT NULL,
    updated_at     TIMESTAMP with time zone,
    created_at     TIMESTAMP with time zone NOT NULL,
    user_id        UUID                     NOT NULL,
    last_active_at TIMESTAMP with time zone NOT NULL,
    CONSTRAINT pk_user_statuses PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID                     NOT NULL,
    updated_at TIMESTAMP with time zone,
    created_at TIMESTAMP with time zone NOT NULL,
    username   VARCHAR(50)              NOT NULL,
    email      VARCHAR(100)             NOT NULL,
    password   VARCHAR(60)              NOT NULL,
    profile_id UUID,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE read_statuses
    ADD CONSTRAINT uc_3fd6e4741dcb586be6de40913 UNIQUE (user_id, channel_id);

ALTER TABLE message_attachments
    ADD CONSTRAINT uc_message_attachments_attachment UNIQUE (attachment_id);

ALTER TABLE user_statuses
    ADD CONSTRAINT uc_user_statuses_user UNIQUE (user_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_profile UNIQUE (profile_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE read_statuses
    ADD CONSTRAINT FK_READ_STATUSES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE read_statuses
    ADD CONSTRAINT FK_READ_STATUSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_PROFILE FOREIGN KEY (profile_id) REFERENCES binary_contents (id);

ALTER TABLE user_statuses
    ADD CONSTRAINT FK_USER_STATUSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE message_attachments
    ADD CONSTRAINT fk_mesatt_on_binary_content FOREIGN KEY (attachment_id) REFERENCES binary_contents (id);

ALTER TABLE message_attachments
    ADD CONSTRAINT fk_mesatt_on_message FOREIGN KEY (message_id) REFERENCES messages (id);
CREATE TABLE binary_contents
(
    id           UUID                     NOT NULL,
    created_at   TIMESTAMP with time zone NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL,
    CONSTRAINT pk_binary_contents PRIMARY KEY (id)
);

CREATE TABLE channels
(
    id          UUID                     NOT NULL,
    updated_at  TIMESTAMP with time zone,
    created_at  TIMESTAMP with time zone NOT NULL,
    type        VARCHAR(255)             NOT NULL,
    name        VARCHAR(100),
    description VARCHAR(500),
    CONSTRAINT pk_channels PRIMARY KEY (id)
);

CREATE TABLE message_attachments
(
    attachment_id UUID NOT NULL,
    message_id    UUID NOT NULL
);

CREATE TABLE messages
(
    id         UUID                     NOT NULL,
    updated_at TIMESTAMP with time zone,
    created_at TIMESTAMP with time zone NOT NULL,
    content    TEXT                     NOT NULL,
    channel_id UUID                     NOT NULL,
    author_id  UUID,
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

CREATE TABLE read_statuses
(
    id           UUID                     NOT NULL,
    updated_at   TIMESTAMP with time zone,
    created_at   TIMESTAMP with time zone NOT NULL,
    user_id      UUID                     NOT NULL,
    channel_id   UUID                     NOT NULL,
    last_read_at TIMESTAMP with time zone NOT NULL,
    CONSTRAINT pk_read_statuses PRIMARY KEY (id)
);

CREATE TABLE user_statuses
(
    id             UUID                     NOT NULL,
    updated_at     TIMESTAMP with time zone,
    created_at     TIMESTAMP with time zone NOT NULL,
    user_id        UUID                     NOT NULL,
    last_active_at TIMESTAMP with time zone NOT NULL,
    CONSTRAINT pk_user_statuses PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID                     NOT NULL,
    updated_at TIMESTAMP with time zone,
    created_at TIMESTAMP with time zone NOT NULL,
    username   VARCHAR(50)              NOT NULL,
    email      VARCHAR(100)             NOT NULL,
    password   VARCHAR(60)              NOT NULL,
    profile_id UUID,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE read_statuses
    ADD CONSTRAINT uc_3fd6e4741dcb586be6de40913 UNIQUE (user_id, channel_id);

ALTER TABLE message_attachments
    ADD CONSTRAINT uc_message_attachments_attachment UNIQUE (attachment_id);

ALTER TABLE user_statuses
    ADD CONSTRAINT uc_user_statuses_user UNIQUE (user_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_profile UNIQUE (profile_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE read_statuses
    ADD CONSTRAINT FK_READ_STATUSES_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channels (id);

ALTER TABLE read_statuses
    ADD CONSTRAINT FK_READ_STATUSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_PROFILE FOREIGN KEY (profile_id) REFERENCES binary_contents (id);

ALTER TABLE user_statuses
    ADD CONSTRAINT FK_USER_STATUSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE message_attachments
    ADD CONSTRAINT fk_mesatt_on_binary_content FOREIGN KEY (attachment_id) REFERENCES binary_contents (id);

ALTER TABLE message_attachments
    ADD CONSTRAINT fk_mesatt_on_message FOREIGN KEY (message_id) REFERENCES messages (id);