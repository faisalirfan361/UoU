-- Add internal auth method for internal calendars that are backed by Nylas Virtual calendars. -->

ALTER TYPE auth_method ADD VALUE 'internal';
