CREATE INDEX idx_launch_note_user_updated_id
    ON launch_note (user_id, updated_at DESC, id DESC);