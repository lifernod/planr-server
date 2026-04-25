CREATE TABLE events
(
    id          UUID                     DEFAULT pg_catalog.gen_random_uuid() NOT NULL,
    creator_id  UUID                                                          NOT NULL,
    type        VARCHAR(128),
    location    TEXT                                                          NOT NULL,
    title       VARCHAR(128)                                                  NOT NULL,
    description VARCHAR(1024),
    start_time  TIMESTAMP WITH TIME ZONE                                      NOT NULL,
    end_time    TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT now(),
    version BIGINT,
    CONSTRAINT pk_events PRIMARY KEY (id)
);

CREATE INDEX
    idx_events_creator_id ON events (creator_id);

ALTER TABLE events
    ADD CONSTRAINT fk_event_creator
        FOREIGN KEY (creator_id) REFERENCES users (id) ON DELETE CASCADE;