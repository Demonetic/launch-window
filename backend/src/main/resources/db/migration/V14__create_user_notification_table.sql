CREATE TABLE user_notification
(
    id                     BIGINT      NOT NULL AUTO_INCREMENT,
    recipient_id           BIGINT      NOT NULL,
    actor_id               BIGINT      NOT NULL,
    type                   VARCHAR(40) NOT NULL,
    friendship_id          BIGINT      NULL,
    calendar_invitation_id BIGINT      NULL,
    created_at             TIMESTAMP(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    read_at                TIMESTAMP(6) NULL,

    CONSTRAINT pk_user_notification
        PRIMARY KEY (id),

    CONSTRAINT fk_notification_recipient
        FOREIGN KEY (recipient_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_notification_actor
        FOREIGN KEY (actor_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_notification_friendship
        FOREIGN KEY (friendship_id)
            REFERENCES friendship (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_notification_calendar_invitation
        FOREIGN KEY (calendar_invitation_id)
            REFERENCES calendar_invitation (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_notification_users
        CHECK (recipient_id <> actor_id),

    CONSTRAINT ck_notification_reference
        CHECK (
            (
                friendship_id IS NOT NULL
                    AND calendar_invitation_id IS NULL
                )
                OR
            (
                friendship_id IS NULL
                    AND calendar_invitation_id IS NOT NULL
                )
            ),

    CONSTRAINT ck_notification_type
        CHECK (
            type IN (
                     'FRIEND_REQUEST_RECEIVED',
                     'FRIEND_REQUEST_ACCEPTED',
                     'FRIEND_REQUEST_DECLINED',
                     'CALENDAR_INVITATION_RECEIVED',
                     'CALENDAR_INVITATION_ACCEPTED',
                     'CALENDAR_INVITATION_DECLINED'
                )
            ),

    CONSTRAINT ck_notification_reference_type
        CHECK (
            (
                friendship_id IS NOT NULL
                    AND type IN (
                                 'FRIEND_REQUEST_RECEIVED',
                                 'FRIEND_REQUEST_ACCEPTED',
                                 'FRIEND_REQUEST_DECLINED'
                    )
                )
                OR
            (
                calendar_invitation_id IS NOT NULL
                    AND type IN (
                                 'CALENDAR_INVITATION_RECEIVED',
                                 'CALENDAR_INVITATION_ACCEPTED',
                                 'CALENDAR_INVITATION_DECLINED'
                    )
                )
            )
);

CREATE INDEX idx_notification_recipient_read_created
    ON user_notification (
                          recipient_id,
                          read_at,
                          created_at,
                          id
        );