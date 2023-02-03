-- Add calendar external_id in prep for internal calendars that sync to Nylas Virtual calendars.
-- Old calendar ids will become the external_ids, so for existing calendars, the ids will match.
-- New ids will be uuids, but stored as strings so we can also support the old ids as well.
-- Ideally, we should later convert the old ids to uuids and change the id column to a real uuid
-- because uuid columns are a bit more efficient and would make sure all new ids are uuids.

ALTER TABLE calendar
  ADD COLUMN external_id VARCHAR(36) UNIQUE;

UPDATE calendar
SET external_id = id;
