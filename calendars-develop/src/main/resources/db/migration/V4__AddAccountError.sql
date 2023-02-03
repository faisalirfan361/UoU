CREATE TYPE account_error_type as ENUM (
  'auth'
  );

CREATE table account_error
(
  id         UUID PRIMARY KEY,
  account_id VARCHAR(36) REFERENCES account (id),
  created_at created_at,
  type       account_error_type NOT NULL,
  message    varchar(1024) NOT NULL,
  details    varchar(1024)
);
CREATE INDEX account_error_account_id_idx ON account_error (account_id);
