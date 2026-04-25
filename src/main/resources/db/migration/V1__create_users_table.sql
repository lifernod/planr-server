CREATE TABLE users
(
    id          UUID                     DEFAULT pg_catalog.gen_random_uuid() NOT NULL,
    name        VARCHAR(128)                                                  NOT NULL,
    role        VARCHAR(20)              DEFAULT 'USER'                       NOT NULL,
    vk_id       VARCHAR(128),
    vk_username VARCHAR(128),
    tg_id       VARCHAR(128),
    tg_username VARCHAR(128),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT now(),
    version BIGINT,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE INDEX
    idx_users_vk_id ON users (vk_id);
CREATE INDEX
    idx_users_tg_id ON users (tg_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_tg UNIQUE (tg_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_vk UNIQUE (vk_id);