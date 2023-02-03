CREATE TYPE auth_method as ENUM (
  'ms-oauth-sa'
  );

CREATE TYPE participant_status AS ENUM (
  'noreply',
  'yes',
  'no',
  'maybe'
  );

CREATE TYPE event_status AS ENUM (
  'confirmed',
  'tentative',
  'cancelled'
  );

CREATE TYPE nylas_account_sync_state as ENUM ('downloading', 'exception', 'initializing',
  'invalid-credentials', 'running', 'stopped', 'sync-error');

CREATE DOMAIN email AS VARCHAR(254);
CREATE DOMAIN created_at AS TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE DOMAIN updated_at AS TIMESTAMPTZ NULL;
CREATE DOMAIN org_id AS VARCHAR(36) NOT NULL; -- Add to main entities to segregate data
CREATE DOMAIN timezone_name AS VARCHAR(75);

CREATE TABLE service_account
(
  id                 UUID PRIMARY KEY,
  org_id             org_id,
  auth_method        auth_method NOT NULL,
  email              email NOT NULL UNIQUE,
  settings_encrypted BYTEA NOT NULL,
  created_at         created_at,
  updated_at         updated_at
);

CREATE TABLE account
(
  id                     VARCHAR(36) PRIMARY KEY,
  org_id                 org_id,
  service_account_id     UUID REFERENCES service_account (id),
  email                  email NOT NULL UNIQUE,
  name                   VARCHAR(250) NOT NULL,
  access_token_encrypted BYTEA NOT NULL,
  nylas_sync_state       nylas_account_sync_state NULL,
  linked_at              TIMESTAMPTZ NOT NULL,
  created_at             created_at,
  updated_at             updated_at
);

CREATE TABLE calendar
(
  id           VARCHAR(36) PRIMARY KEY,
  org_id       org_id,
  account_id   VARCHAR(36) REFERENCES account (id),
  name         VARCHAR(250) NOT NULL,
  timezone     timezone_name,
  is_read_only BOOLEAN NOT NULL DEFAULT FALSE,
  created_at   created_at,
  updated_at   updated_at
);
CREATE INDEX calendar_org_id_idx ON calendar (org_id);

CREATE TABLE event
(
  id                 UUID PRIMARY KEY,
  org_id             org_id,
  external_id        VARCHAR(36) UNIQUE,
  ical_uid           VARCHAR(254) UNIQUE,
  master_event_id    UUID,
  master_external_id VARCHAR(36),
  calendar_id        VARCHAR(36) NOT NULL REFERENCES calendar (id),
  title              VARCHAR(1024) NOT NULL,
  description        VARCHAR(8192),
  location           VARCHAR(500),
  start_at           TIMESTAMPTZ NOT NULL,
  start_timezone     timezone_name,
  end_at             TIMESTAMPTZ NOT NULL,
  end_timezone       timezone_name,
  is_all_day         BOOLEAN NOT NULL DEFAULT FALSE,
  all_day_start_at   DATE,
  all_day_end_at     DATE,
  is_read_only       BOOLEAN NOT NULL DEFAULT FALSE,
  owner_name         VARCHAR(250),
  owner_email        email,
  status             event_status,
  recurrence         JSON,
  checkin_at         TIMESTAMPTZ,
  checkout_at        TIMESTAMPTZ,
  created_at         created_at,
  updated_at         updated_at
);
COMMENT ON COLUMN event.start_at IS 'Exact point in time, which for all-day events is '
  'interpreted from all_day_start_at using calendar.timezone';
COMMENT ON COLUMN event.end_at IS 'Exact point in time, which for all-day events is '
  'interpreted from all_day_end_at using calendar.timezone';
CREATE INDEX event_org_id_idx ON event (org_id);
CREATE INDEX event_calendar_id_idx ON event (calendar_id);
CREATE INDEX event_start_at_end_at_idx on event (start_at, end_at);

CREATE TABLE auth_code
(
  code         UUID PRIMARY KEY,
  org_id       org_id,
  expiration   TIMESTAMPTZ NOT NULL,
  redirect_uri VARCHAR(2000)
);

CREATE TABLE participant
(
  event_id UUID NOT NULL REFERENCES event (id),
  email    email NOT NULL,
  name     VARCHAR(250),
  status   participant_status NOT NULL DEFAULT 'noreply',
  comment  VARCHAR(1000),
  PRIMARY KEY (event_id, email)
)
