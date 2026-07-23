ALTER TABLE app_user
    ADD COLUMN avatar_key VARCHAR(30) NOT NULL DEFAULT 'ASTRONAUT',
    ADD COLUMN avatar_color VARCHAR(7) NOT NULL DEFAULT '#FFFFFF';

ALTER TABLE app_user
    ADD CONSTRAINT ck_app_user_avatar_key CHECK (
        avatar_key IN (
                       'ASTRONAUT',
                       'ALIEN',
                       'MOON_BASE_ROBOT',
                       'ROCKET',
                       'SATELLITE',
                       'PLANET',
                       'LUNAR_ROVER',
                       'TELESCOPE'
            )
        );