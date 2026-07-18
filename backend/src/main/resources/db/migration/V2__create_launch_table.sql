CREATE TABLE launch
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    external_id       VARCHAR(100)   NOT NULL,
    name              VARCHAR(255)   NOT NULL,
    description       TEXT           NULL,
    status            VARCHAR(50)    NOT NULL,
    launch_time       DATETIME(6)    NOT NULL,
    image_url         TEXT           NULL,
    webcast_url       TEXT           NULL,
    rocket_name       VARCHAR(255)   NOT NULL,
    mission_type      VARCHAR(100)   NULL,
    organization_name VARCHAR(255)   NULL,
    pad_name          VARCHAR(255)   NULL,
    location_name     VARCHAR(255)   NULL,
    latitude          DECIMAL(9, 6)  NULL,
    longitude         DECIMAL(9, 6)  NULL,
    last_synced_at    DATETIME(6)    NOT NULL,

    CONSTRAINT pk_launch PRIMARY KEY (id),
    CONSTRAINT uk_launch_external_id UNIQUE (external_id),
    CONSTRAINT ck_launch_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_launch_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);