-- Seed data for local dev.
-- This is a repeatable migration, so make all SQL repeatable.
-- If we add much here, we'll probably want to convert to a java migration so it's easy to maintain.

INSERT INTO account (id, org_id, email, name, access_token_encrypted, nylas_sync_state, auth_method, linked_at, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'test', 'test1@test', 'Test User 1', '', 'running', 'ms-oauth-sa', NOW(), NOW(), NULL),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'test', 'test2@test', 'Test User 2', '', 'running', 'ms-oauth-sa', NOW(), NOW(), NULL),
       ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'test', 'test3@test', 'Test User 3', '', 'running', 'google-oauth', NOW(), NOW(), NULL)
ON CONFLICT DO NOTHING;

INSERT INTO account_error (id, account_id, created_at, type, message, details)
VALUES (gen_random_uuid(), 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), 'auth', 'auth error 1', 'details'),
       (gen_random_uuid(), 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(), 'auth', 'auth error 2', 'details');

INSERT INTO calendar (id, external_id, org_id, account_id, name, timezone, created_at, updated_at)
VALUES ('a1000000-0000-0000-0000-000000000000', 'test-a1-ext', 'test', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Test a1', 'America/Denver', NOW(), NULL),
       ('a2000000-0000-0000-0000-000000000000', 'test-a2-ext', 'test', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Test a2', 'America/Denver', NOW(), NULL),
       ('a3000000-0000-0000-0000-000000000000', 'test-a3-ext', 'test', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Test a3', 'America/Denver', NOW(), NULL),
       ('b1000000-0000-0000-0000-000000000000', 'test-b1-ext', 'test', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Test b1', 'America/Chicago', NOW(), NULL),
       ('b2000000-0000-0000-0000-000000000000', 'test-b2-ext', 'test', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Test b2', 'America/Chicago', NOW(), NULL),
       ('b3000000-0000-0000-0000-000000000000', 'test-b3-ext', 'test', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Test b3', 'America/Chicago', NOW(), NULL)
ON CONFLICT DO NOTHING;

-- Regular events:
INSERT INTO event (id, org_id, external_id, ical_uid, calendar_id, title,
                   description, location, start_at,  end_at,  is_read_only, created_at)
VALUES ('10000000-0000-0000-0000-000000000000', 'test', '1-ext', '1-ical', 'a1000000-0000-0000-0000-000000000000', 'Event a1-1',
        'test', 'test', '2022-03-01T10:00:00Z',  '2022-03-01T11:00:00Z',  false, NOW()),
       ('20000000-0000-0000-0000-000000000000', 'test', '2-ext', '2-ical', 'a1000000-0000-0000-0000-000000000000', 'Event a1-2',
        'test', 'test', '2022-03-01T11:00:00Z',  '2022-03-01T11:15:00Z',  false, NOW()),
       ('30000000-0000-0000-0000-000000000000', 'test', '3-ext', '3-ical', 'a1000000-0000-0000-0000-000000000000', 'Event a1-3',
        'test', 'test', '2022-03-01T11:15:00Z',  '2022-03-01T11:30:00Z',  false, NOW())
ON CONFLICT DO NOTHING;

-- All-day events (start_at, end_at are interpreted using calendar timezone):
INSERT INTO event (id, org_id, external_id, ical_uid, calendar_id, title,
                   description, location, all_day_start_at, all_day_end_at, start_at, end_at, is_all_day, is_read_only, created_at)
VALUES ('a1000000-0000-0000-0000-000000000000', 'test', 'a1-ext', 'a1-ical', 'a1000000-0000-0000-0000-000000000000', 'Event a1-a1',
        'test', 'test', '2022-03-01', '2022-03-01', '2022-03-01 America/Denver', '2022-03-02 America/Denver', true, false, NOW()),
       ('a2000000-0000-0000-0000-000000000000', 'test', 'a2-ext', 'a2-ical', 'a2000000-0000-0000-0000-000000000000', 'Event a1-a2',
        'test', 'test', '2022-03-01', '2022-03-05', '2022-03-01 America/Denver', '2022-03-06 America/Denver', true, false, NOW()),
       ('a3000000-0000-0000-0000-000000000000', 'test', 'a3-ext', 'a3-ical', 'a3000000-0000-0000-0000-000000000000', 'Event a1-a3',
        'test', 'test', '2022-03-05', '2022-03-10', '2022-03-05 America/Denver', '2022-03-11 America/Denver', true, false, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO participant (event_id, name, email, status, comment)
VALUES ('10000000-0000-0000-0000-000000000000', 'Participant1 a1-1', 'participant1.a1-1@test.com', 'yes', NULL),
       ('10000000-0000-0000-0000-000000000000', 'Participant2 a1-1', 'participant2.a1-1@test.com', 'yes', NULL),
       ('20000000-0000-0000-0000-000000000000', 'Participant1 a1-2', 'participant1.a1-2@test.com', 'maybe', NULL),
       ('20000000-0000-0000-0000-000000000000', 'Participant2 a1-2', 'participant2.a1-2@test.com', 'no', NULL),
       ('30000000-0000-0000-0000-000000000000', 'Participant1 a1-3', 'participant1.a1-3@test.com', 'noreply', NULL),
       ('30000000-0000-0000-0000-000000000000', 'Participant2 a1-3', 'participant2.a1-3@test.com', 'noreply', NULL)
ON CONFLICT DO NOTHING;

-- Insert some auth codes that will never expire for testing:
INSERT INTO auth_code (code, org_id, expiration)
VALUES ('00000000-0000-0000-0000-000000000000', 'test', '3000-01-01'),
       ('10000000-0000-0000-0000-000000000000', 'test', '3000-01-01'),
       ('20000000-0000-0000-0000-000000000000', 'test', '3000-01-01'),
       ('30000000-0000-0000-0000-000000000000', 'other-org', '3000-01-01')
ON CONFLICT DO NOTHING;
