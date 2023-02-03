-- Makes calendar timezone non-null (if not provided, we'll use a default at create time) -->

-- Set any existing calendars without timezones to UTC, which is the default.
UPDATE calendar
SET timezone = 'UTC'
WHERE timezone IS NULL OR timezone = '';

ALTER TABLE calendar
  ALTER COLUMN timezone SET NOT NULL;
