-- Remove time zone from TimeSpan (start/end date) -->

ALTER TABLE event
  DROP COLUMN start_timezone ;

ALTER TABLE event
  DROP COLUMN end_timezone ;
