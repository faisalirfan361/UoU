-- Adds table for conferencing user accounts (Teams/Zoom)

CREATE TYPE conferencing_auth_method as ENUM (
  'conf-teams-oauth'
);

CREATE TABLE conferencing_user
(
  id                      UUID PRIMARY KEY,
  org_id                  org_id,
  auth_method             conferencing_auth_method NOT NULL,
  email                   email NOT NULL,
  name                    VARCHAR(250) NOT NULL,
  refresh_token_encrypted BYTEA NOT NULL,
  access_token_encrypted  BYTEA NOT NULL,
  expire_at               TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at              created_at,
  updated_at              updated_at,
  UNIQUE (email, org_id)
);
