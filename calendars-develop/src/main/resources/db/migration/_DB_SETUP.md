# DB setup instructions

When using flyway locally, migrations can be run exactly as is with the superuser / root user.
For qa, prod, or any environment where you need to manually setup the db first, follow the
steps below.

## Step 1: Access the DB server.

This will depend on how the DB is setup by devops. You may be able to access the DB from
your machine on a VPN, and if so, just use the details devops gave you. Or, there may be
some scenarios where you need to access the DB via a k8s pod in a cluster that allows access.

You can use something like below to access a newly created DB server.
```
apt install -y postgresql-client
psql -U <admin-username> -d postgres -h <db-server>
```

## Step 2: Create the DB and extensions.

Once connected, the first step is to create the DB if needed (unless devops did it).

```sql
-- Run as root/super user.
CREATE DATABASE calendars;
```

## Step 3: Create users.

Create the users that will be used for migrations and the app. The calendars_admin user will be
used for migrations, and calendars_user will be for the app. Put the generated passwords somewhere
secure so they'll only be available to the running app (DON'T CHECK THEM IN!).

```sql
-- Run as root/super user.
CREATE USER calendars_admin WITH ENCRYPTED PASSWORD '<generate-a-secure-password>';
GRANT ALL ON DATABASE calendars TO calendars_admin;
CREATE USER calendars_user WITH ENCRYPTED PASSWORD '<generate-a-secure-password>';
```

## Step 4: Grant default privileges.

Grant default privileges as calendars_admin. You have to run `ALTER DEFAULT PRIVILEGES` as the
user who will run migrations, which is calendars_admin. This is what allows calendars_admin to
create new tables and give automatic privileges for calendars_user.

```sql
-- Run as calendars_admin (important!).
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO calendars_user;
```
