-- Adjust field lengths to match Nylas requirements (most already matched) -->

-- Truncate any location values that are too long. This won't affect anything valid because we're
-- just matching the Nylas limit; therefore, anything over would have failed to sync anyway.
UPDATE event
SET location = substr(location, 1, 255)
where length(location) > 255;

-- Now reduce field length.
ALTER TABLE event
  ALTER COLUMN location TYPE VARCHAR(255);
