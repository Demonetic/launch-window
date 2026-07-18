CREATE TABLE weather_snapshot
(
    id                                BIGINT        NOT NULL AUTO_INCREMENT,
    launch_id                         BIGINT        NOT NULL,
    forecast_time                     DATETIME(6)   NOT NULL,
    temperature_c                     DECIMAL(5, 2) NOT NULL,
    cloud_cover_percent               SMALLINT      NOT NULL,
    precipitation_probability_percent SMALLINT      NOT NULL,
    wind_speed_kmh                     DECIMAL(6, 2) NOT NULL,
    visibility_meters                 INTEGER       NULL,
    viewing_score                     SMALLINT      NOT NULL,
    fetched_at                        DATETIME(6)    NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_weather_snapshot PRIMARY KEY (id),
    CONSTRAINT uk_weather_snapshot_launch_forecast
        UNIQUE (launch_id, forecast_time),
    CONSTRAINT fk_weather_snapshot_launch
        FOREIGN KEY (launch_id) REFERENCES launch (id)
            ON DELETE CASCADE,
    CONSTRAINT ck_weather_snapshot_cloud_cover
        CHECK (cloud_cover_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_weather_snapshot_precipitation
        CHECK (precipitation_probability_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_weather_snapshot_wind_speed
        CHECK (wind_speed_kmh >= 0),
    CONSTRAINT ck_weather_snapshot_visibility
        CHECK (visibility_meters IS NULL OR visibility_meters >= 0),
    CONSTRAINT ck_weather_snapshot_viewing_score
        CHECK (viewing_score BETWEEN 0 AND 100)
);