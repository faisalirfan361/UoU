-- Changes for recurring events and event expansion -->

-- Make external id longer because we weren't accounting for recurring id suffixes that get added.
-- For example: abixy0enus49cj5jlqdiov4kt_20220502T140000Z
-- We'll allow plenty extra chars to be safe because we don't know what Nylas will do in the future.
ALTER TABLE event
  ALTER COLUMN external_id TYPE VARCHAR(72);

-- Make ical_uid non-unique because recurring event instances may share the same value.
-- We probably won't be doing much with this id, so we shouldn't need an index at all.
ALTER TABLE event
  DROP CONSTRAINT event_ical_uid_key;

-- Make sure parent/child recurring references are valid with foreign key, and rename column
-- to be more clearly related to recurrence and not be confusing with Nylas master_event_id which
-- is actually the external id, not our id. Also drop master_external_id altogether because it can
-- be fetched if needed, and probably won't be much, and then we don't need a compound foreign key
-- to ensure the master id and external_id match.
ALTER TABLE event
  RENAME COLUMN master_event_id TO recurrence_master_id;
ALTER TABLE event
  ADD CONSTRAINT event_recurrence_master_id_fkey
    FOREIGN KEY (recurrence_master_id) REFERENCES event (id);
ALTER TABLE event
  DROP COLUMN master_external_id;

-- Add a column to track whether an event is a recurring instance override.
ALTER TABLE event
  ADD COLUMN is_recurrence_override BOOL NOT NULL DEFAULT FALSE;
