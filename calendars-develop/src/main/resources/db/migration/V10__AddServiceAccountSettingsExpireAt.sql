-- Add service account settings expiration so we can expire and refresh MS OAuth refresh tokens -->

ALTER TABLE service_account
  ADD COLUMN settings_expire_at TIMESTAMPTZ NULL;
