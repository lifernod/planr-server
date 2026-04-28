CREATE TABLE event_participants
(
    event_id UUID NOT NULL,
    role     VARCHAR(32) DEFAULT 'PARTICIPANT',
    user_id  UUID NOT NULL,
    CONSTRAINT pk_event_participants PRIMARY KEY (event_id, user_id)
);

CREATE INDEX idx_event_participant_user
    ON event_participants (user_id);

ALTER TABLE event_participants
    ADD CONSTRAINT fk_event_participants_event
        FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_event_participants_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;