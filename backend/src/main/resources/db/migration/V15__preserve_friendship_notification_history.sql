ALTER TABLE user_notification
    ADD COLUMN friendship_status VARCHAR(20) NULL
        AFTER friendship_id;

UPDATE user_notification notification
    JOIN friendship friendship
ON friendship.id = notification.friendship_id
    SET notification.friendship_status = friendship.status
WHERE notification.friendship_id IS NOT NULL;

ALTER TABLE user_notification
DROP FOREIGN KEY fk_notification_friendship;

ALTER TABLE user_notification
DROP CHECK ck_notification_reference;

ALTER TABLE user_notification
DROP CHECK ck_notification_reference_type;

ALTER TABLE user_notification
    ADD CONSTRAINT fk_notification_friendship
        FOREIGN KEY (friendship_id)
            REFERENCES friendship (id)
            ON DELETE SET NULL;

ALTER TABLE user_notification
    ADD CONSTRAINT ck_notification_reference
        CHECK (
            (
                friendship_status IS NOT NULL
                    AND calendar_invitation_id IS NULL
                )
                OR
            (
                friendship_status IS NULL
                    AND calendar_invitation_id IS NOT NULL
                )
            );

ALTER TABLE user_notification
    ADD CONSTRAINT ck_notification_reference_type
        CHECK (
            (
                friendship_status IS NOT NULL
                    AND calendar_invitation_id IS NULL
                    AND type IN (
                                 'FRIEND_REQUEST_RECEIVED',
                                 'FRIEND_REQUEST_ACCEPTED',
                                 'FRIEND_REQUEST_DECLINED'
                    )
                )
                OR
            (
                friendship_status IS NULL
                    AND calendar_invitation_id IS NOT NULL
                    AND type IN (
                                 'CALENDAR_INVITATION_RECEIVED',
                                 'CALENDAR_INVITATION_ACCEPTED',
                                 'CALENDAR_INVITATION_DECLINED'
                    )
                )
            );

ALTER TABLE user_notification
    ADD CONSTRAINT ck_notification_friendship_status
        CHECK (
            friendship_status IS NULL
                OR friendship_status IN (
                                         'PENDING',
                                         'ACCEPTED',
                                         'DECLINED'
                )
            );