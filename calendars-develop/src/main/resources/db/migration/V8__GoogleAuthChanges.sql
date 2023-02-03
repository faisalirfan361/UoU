-- Changes for Google auth and normal account auth in general (vs. service accounts) -->

-- Add an enum value for Google OAuth auth method
ALTER TYPE auth_method ADD VALUE 'google-oauth';

-- Add an enum value for the "partial" nylas sync state. This status IS NOT documented by Nylas as a
-- possible option for the accounts API, although it is documented as a dashboard status. We see it
-- often for newly connected Google accounts, and it means basically "running" but some stuff is
-- still syncing.
ALTER TYPE nylas_account_sync_state ADD VALUE 'partial';

-- Add auth method to accounts, filling existing rows with the only previously supported method.
ALTER TABLE account ADD COLUMN auth_method auth_method NULL;
UPDATE account SET auth_method = 'ms-oauth-sa';
ALTER TABLE account ALTER COLUMN auth_method SET NOT NULL;
