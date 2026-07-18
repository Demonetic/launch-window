CREATE TABLE calendar_entry
(
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    user_id   BIGINT      NOT NULL,
    launch_id BIGINT      NOT NULL,
    saved_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_calendar_entry PRIMARY KEY (id),
    CONSTRAINT uk_calendar_entry_user_launch UNIQUE (user_id, launch_id),
    CONSTRAINT fk_calendar_entry_user
        FOREIGN KEY (user_id) REFERENCES app_user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_calendar_entry_launch
        FOREIGN KEY (launch_id) REFERENCES launch (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_calendar_entry_launch_id
    ON calendar_entry (launch_id);