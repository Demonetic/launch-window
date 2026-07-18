CREATE TABLE launch_note
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    launch_id  BIGINT      NOT NULL,
    content    TEXT        NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_launch_note PRIMARY KEY (id),
    CONSTRAINT fk_launch_note_user
        FOREIGN KEY (user_id) REFERENCES app_user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_launch_note_launch
        FOREIGN KEY (launch_id) REFERENCES launch (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_launch_note_user_launch
    ON launch_note (user_id, launch_id);

CREATE INDEX idx_launch_note_launch_id
    ON launch_note (launch_id);