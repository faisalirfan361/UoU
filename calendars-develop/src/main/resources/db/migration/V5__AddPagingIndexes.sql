-- Add indexes that are missing for cursor pagination and associated sorting.
CREATE INDEX calendar_created_at_idx ON calendar (created_at);
CREATE INDEX participant_email_idx ON participant (email);
