-- Add new fields to track the data source for event creates and updates. -->

ALTER TABLE event
  ADD COLUMN created_from VARCHAR(50),
  ADD COLUMN updated_from VARCHAR(50);
