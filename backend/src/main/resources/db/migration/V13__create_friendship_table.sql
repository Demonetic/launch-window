CREATE TABLE friendship
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    first_user_id  BIGINT      NOT NULL,
    second_user_id BIGINT      NOT NULL,
    requester_id   BIGINT      NOT NULL,
    status         VARCHAR(20) NOT NULL,
    created_at     TIMESTAMP(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    responded_at   TIMESTAMP(6) NULL,

    CONSTRAINT pk_friendship
        PRIMARY KEY (id),

    CONSTRAINT uk_friendship_user_pair
        UNIQUE (first_user_id, second_user_id),

    CONSTRAINT fk_friendship_first_user
        FOREIGN KEY (first_user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_friendship_second_user
        FOREIGN KEY (second_user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_friendship_requester
        FOREIGN KEY (requester_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_friendship_user_order
        CHECK (first_user_id < second_user_id),

    CONSTRAINT ck_friendship_requester
        CHECK (
            requester_id = first_user_id
                OR requester_id = second_user_id
            ),

    CONSTRAINT ck_friendship_status
        CHECK (
            status IN (
                       'PENDING',
                       'ACCEPTED',
                       'DECLINED'
                )
            )
);

CREATE INDEX idx_friendship_first_user_status
    ON friendship (
                   first_user_id,
                   status,
                   created_at,
                   id
        );

CREATE INDEX idx_friendship_second_user_status
    ON friendship (
                   second_user_id,
                   status,
                   created_at,
                   id
        );