-- 1. 테이블 삭제
-- DROP TABLE IF EXISTS users CASCADE;
-- DROP TABLE IF EXISTS user_statuses CASCADE;
-- DROP TABLE IF EXISTS channels CASCADE;
-- DROP TABLE IF EXISTS messages CASCADE;
-- DROP TABLE IF EXISTS read_statuses CASCADE;
-- DROP TABLE IF EXISTS binary_contents CASCADE;
-- DROP TABLE IF EXISTS message_attachments CASCADE;

-- 2. 테이블 생성
CREATE TABLE IF NOT EXISTS binary_contents
(
    -- column level constraints
    id           uuid,
    created_at   timestamptz  NOT NULL,
    file_name    varchar(255) NOT NULL,
    size         bigint       NOT NULL,
    content_type varchar(100) NOT NULL,
    -- table level constraints
    CONSTRAINT pk_binary_contents_id PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS channels
(
    -- column level constraints
    id          uuid        NOT NULL,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz,
    name        varchar(100),
    description varchar(500),
    type        varchar(10) NOT NULL,
    -- table level constraints
    CONSTRAINT pk_channels_id PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users
(
    -- column level constraints
    id         uuid         NOT NULL,
    created_at timestamptz  NOT NULL,
    updated_at timestamptz,
    username   varchar(50)  NOT NULL,
    email      varchar(100) NOT NULL,
    password   varchar(60)  NOT NULL,
    profile_id uuid,
    -- table level constraints
    CONSTRAINT pk_users_id PRIMARY KEY (id),
    CONSTRAINT uk_users_name UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_profile_id FOREIGN KEY (profile_id) REFERENCES binary_contents (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_statuses
(
    -- column level constraints
    id             uuid        NOT NULL,
    created_at     timestamptz NOT NULL,
    updated_at     timestamptz,
    user_id        uuid        NOT NULL,
    last_active_at timestamptz NOT NULL,
    -- table level constraints
    CONSTRAINT pk_user_statuses_id PRIMARY KEY (id),
    CONSTRAINT fk_user_statuses_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_statuses_user_id UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS messages
(
    -- column level constraints
    id         uuid        NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz,
    content    text,
    channel_id uuid        NOT NULL,
    author_id  uuid,
    -- table level constraints
    CONSTRAINT pk_messages_id PRIMARY KEY (id),
    CONSTRAINT fk_messages_channel_id FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_author_id FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS read_statuses
(
    -- column level constraints
    id           uuid        NOT NULL,
    created_at   timestamptz NOT NULL,
    updated_at   timestamptz,
    user_id      uuid        NOT NULL,
    channel_id   uuid        NOT NULL,
    last_read_at timestamptz NOT NULL,
    -- table level constraints
    CONSTRAINT pk_read_statuses_id PRIMARY KEY (id),
    CONSTRAINT fk_read_statuses_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_read_statues_channel_id FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT uk_read_statuses_user_channel UNIQUE (user_id, channel_id)
);

CREATE TABLE IF NOT EXISTS message_attachments
(
    -- column level constraints
    message_id    uuid NOT NULL,
    attachment_id uuid NOT NULL,
    -- table level constraints
    CONSTRAINT pk_message_id_attachment_id PRIMARY KEY (message_id, attachment_id),
    CONSTRAINT fk_message_attachments_message_id FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_attachment_id FOREIGN KEY (attachment_id) REFERENCES binary_contents (id) ON DELETE CASCADE
);

-- 인덱스 추가
CREATE INDEX idx_messages_channel_created_at ON messages (channel_id, created_at DESC);

-- 테이블 설명 추가
COMMENT ON TABLE users IS '유저';
COMMENT ON TABLE user_statuses IS '유저 상태';
COMMENT ON TABLE channels IS '채널';
COMMENT ON TABLE messages IS '메시지';
COMMENT ON TABLE read_statuses IS '메시지 읽음 상태';
COMMENT ON TABLE binary_contents IS '첨부파일';
COMMENT ON TABLE message_attachments IS '메시지 첨부 파일';

-- 컬럼 설명 추가
COMMENT ON COLUMN users.id IS '유저 코드';
COMMENT ON COLUMN users.created_at IS '유저생성시간';
COMMENT ON COLUMN users.updated_at IS '유저갱신시간';
COMMENT ON COLUMN users.username IS '유저이름';
COMMENT ON COLUMN users.email IS '유저이메일';
COMMENT ON COLUMN users.password IS '유저비밀번호';
COMMENT ON COLUMN users.profile_id IS '유저프로필코드';

COMMENT ON COLUMN user_statuses.id IS '유저상태코드';
COMMENT ON COLUMN user_statuses.created_at IS '유저상태생성시간';
COMMENT ON COLUMN user_statuses.updated_at IS '유저상태갱신시간';
COMMENT ON COLUMN user_statuses.user_id IS '상위 유저 코드';
COMMENT ON COLUMN user_statuses.last_active_at IS '마지막활성화시간';

COMMENT ON COLUMN channels.id IS '채널코드';
COMMENT ON COLUMN channels.created_at IS '채널생성시간';
COMMENT ON COLUMN channels.updated_at IS '채널갱신시간';
COMMENT ON COLUMN channels.name IS '채널이름';
COMMENT ON COLUMN channels.description IS '채널설명';
COMMENT ON COLUMN channels.type IS '채널종류';

COMMENT ON COLUMN messages.id IS '메시지코드';
COMMENT ON COLUMN messages.created_at IS '메시지생성시간';
COMMENT ON COLUMN messages.updated_at IS '메시지갱신시간';
COMMENT ON COLUMN messages.content IS '메시지내용';
COMMENT ON COLUMN messages.channel_id IS '상위 채널코드';
COMMENT ON COLUMN messages.author_id IS '상위 유저코드';

COMMENT ON COLUMN read_statuses.id IS '메시지 읽음 상태코드';
COMMENT ON COLUMN read_statuses.created_at IS '메시지 읽음 상태생성시간';
COMMENT ON COLUMN read_statuses.updated_at IS '메시지 읽음 상태갱신시간';
COMMENT ON COLUMN read_statuses.user_id IS '상위 유저코드';
COMMENT ON COLUMN read_statuses.channel_id IS '상위 채널코드';

COMMENT ON COLUMN binary_contents.id IS '첨부파일코드';
COMMENT ON COLUMN binary_contents.created_at IS '첨부파일생성시간';
COMMENT ON COLUMN binary_contents.file_name IS '첨부파일이름';
COMMENT ON COLUMN binary_contents.size IS '첨부파일크기';
COMMENT ON COLUMN binary_contents.content_type IS '첨부파일타입';

COMMENT ON COLUMN message_attachments.message_id IS '상위 메시지코드';
COMMENT ON COLUMN message_attachments.attachment_id IS '상위 첨부파일코드';
