ALTER TABLE launch
    ADD COLUMN country_code VARCHAR(3) NULL AFTER location_name,
    ADD COLUMN country_name VARCHAR(100) NULL AFTER country_code;

CREATE INDEX idx_launch_country_time
    ON launch (country_code, launch_time, id);