-- 用户表
CREATE TABLE IF NOT EXISTS "user" (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64),
    email         VARCHAR(128),
    avatar_url    VARCHAR(512),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 第三方账号绑定（支持 GitHub / Google 多平台登录）
CREATE TABLE IF NOT EXISTS user_oauth_connection (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    provider        VARCHAR(32) NOT NULL,
    provider_uid    VARCHAR(128) NOT NULL,
    access_token    VARCHAR(512),
    refresh_token   VARCHAR(512),
    token_expires_at TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_provider_uid UNIQUE (provider, provider_uid)
);

-- 生成任务表
CREATE TABLE IF NOT EXISTS generation_task (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT,
    provider        VARCHAR(32) NOT NULL,
    prompt          TEXT NOT NULL,
    negative_prompt TEXT,
    image_url       VARCHAR(512),
    duration        INT DEFAULT 5,
    resolution      VARCHAR(32) DEFAULT '1080p',
    aspect_ratio    VARCHAR(16) DEFAULT '16:9',
    seed            BIGINT,
    status          VARCHAR(32) DEFAULT 'PENDING',
    video_url       VARCHAR(512),
    error_msg       TEXT,
    cost            DECIMAL(10,4),
    provider_task_id VARCHAR(128),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_generation_task_user_id ON generation_task (user_id);
CREATE INDEX IF NOT EXISTS idx_generation_task_status ON generation_task (status);
