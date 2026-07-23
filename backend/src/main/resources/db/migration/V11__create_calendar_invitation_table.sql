CREATE TABLE calendar_invitation
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    calendar_entry_id BIGINT      NOT NULL,
    inviter_id        BIGINT      NOT NULL,
    invitee_id        BIGINT      NOT NULL,
    status            VARCHAR(20) NOT NULL,
    created_at        TIMESTAMP(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    responded_at      TIMESTAMP(6) NULL,

    CONSTRAINT pk_calendar_invitation
        PRIMARY KEY (id),

    CONSTRAINT uk_calendar_invitation_entry_invitee
        UNIQUE (calendar_entry_id, invitee_id),

    CONSTRAINT fk_calendar_invitation_entry
        FOREIGN KEY (calendar_entry_id)
            REFERENCES calendar_entry (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_calendar_invitation_inviter
        FOREIGN KEY (inviter_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_calendar_invitation_invitee
        FOREIGN KEY (invitee_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_calendar_invitation_status
        CHECK (
            status IN (
                       'PENDING',
                       'ACCEPTED',
                       'DECLINED'
                )
            ),

    CONSTRAINT ck_calendar_invitation_users
        CHECK (inviter_id <> invitee_id)
);

CREATE INDEX idx_calendar_invitation_invitee_status_created
    ON calendar_invitation (
                            invitee_id,
                            status,
                            created_at,
                            id
        );