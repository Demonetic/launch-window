DELETE older_snapshot
FROM weather_snapshot older_snapshot
JOIN weather_snapshot newer_snapshot
    ON newer_snapshot.launch_id = older_snapshot.launch_id
    AND (
        newer_snapshot.fetched_at > older_snapshot.fetched_at
        OR (
            newer_snapshot.fetched_at = older_snapshot.fetched_at
            AND newer_snapshot.id > older_snapshot.id
        )
    );

ALTER TABLE weather_snapshot
    ADD CONSTRAINT uk_weather_snapshot_launch
        UNIQUE (launch_id);

ALTER TABLE weather_snapshot
DROP INDEX uk_weather_snapshot_launch_forecast;