-- Add an index to make recurrence instance lookups faster -->

CREATE INDEX event_recurrence_master_id_idx ON event (recurrence_master_id);
