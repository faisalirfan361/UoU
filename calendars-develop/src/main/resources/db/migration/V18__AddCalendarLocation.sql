-- Add location field into Calendar -->


ALTER TABLE calendar
  ADD COLUMN location VARCHAR(500) NULL;
