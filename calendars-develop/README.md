Calendars API
=============

[![Codefresh build status]( https://g.codefresh.io/api/badges/pipeline/spaceiq/UoU%2Fcalendars?type=cf-1&key=eyJhbGciOiJIUzI1NiJ9.NWNiNjc0Y2NiOTY2YzEzMTU3MGJmZjE5.Ji7D83_3qDVLmwdMbxbMS6oxHSnzhV2UGDwEYeS7XAw)]( https://g.codefresh.io/pipelines/edit/new/builds?id=6234dfb2c718107ccc1723fb&pipeline=calendars&projects=UoU&projectId=61b147ded8e622706794c8a3)

This is our Calendars API that can be used for space booking or anything else that requires a calendar.
This API represents calendars in a generic way and *does not* contain any concepts from related
domains such as spaces. Our calendars should work like other calenders, and any other, non-calendar
features should be implemented using this API as a building block.

**See also:**
- [Confluence: Home](https://digitizework.atlassian.net/l/cp/w1TyFa0c)
- [Confluence: Architecture](https://digitizework.atlassian.net/l/cp/Rqc0VU0B)
- [Confluence: Use Cases & Flows](https://digitizework.atlassian.net/l/cp/0Ffuf118)
- [API docs (in QA environment)](https://calendars-default.qa.UoUconnect.com/docs)

----------

## Background

### Major features

- External calendars that sync to calendar providers: Google, Office365, and Exchange
- Internal calendars that exist within our API only (no sync)
- Recurring events using RRULEs
- Availability endpoints to get free/busy info
- Simple UI for authorizing calendar accounts via several login options

### Nylas for calendar sync

All external calendar sync is handled by [Nylas](https://www.nylas.com/products/calendar-api/),
so we avoid any direct communication with Google, Office365, or Exchange. Our domain should closely
match Nylas where possible to make sync easier.

### Components and tools

- Spring Boot: Our overall application framework.
- Docker: For running app dependencies in development and packaging the app for deployment.
- Gradle: For building the app, running tests, etc. (see build.gradle.kts).
- Jooq: For strongly-typed SQL and ORM-ish functionality. We generate types based on the DB.
- Flyway: For database migrations, which run automatically in local development (but not prod).
- Kafka: For background processing and sending messages to other systems.
- Avro: For kafka schemas. We generate record objects from the schemas.
- Redis: For caching mostly, but also for some things related to scheduled tasks.
- MapStruct: For DTO/object mapping. We generate mappers to avoid reflection-based mapping.
- SLF4J w/ Log4j: For logging. Use the SLF4J facade *only*, but under the hood it's Log4j2.
- Testcontainers: For Docker test dependencies in integration tests (db, kafka, etc.).
- REST-assured for API integration tests
- springdoc-openapi: For rendering OpenAPI 3 docs and a Swagger UI.

### Architecture

This app uses a simplified onion-kinda architecture with the layers below. The goal of this setup
is to keep some clear organization without a lot of ceremony and unnecessary passing-through-layers.
If our domain rules or business logic become more complicated, we can evolve this as needed.

- **com.UoU.core**: Contains the domain model, business logic, and application services. This package
  should be free of most infrastructure and API concerns and focus on behavior. All other layers
  depend on this core layer, but this layer should not depend on any others. This layer contains
  interfaces for repositories, etc. that will be implemented for specific technologies in the infra
  layer. Basically, core owns the contract for all models and domain/business stuff, but the
  implementation of these things is not a core concern.

- **com.UoU.infra**: Contains infrastructure-related helpers such as for db access and kafka. Stuff
  in this layer can be thought of as adapters to specific technology implementations. This layer
  should contain as little domain and business logic as possible.

- **com.UoU.app**: Presents the app to users as a REST API using Spring Boot. This layer is the
  entrypoint that sets up DI and orchestrates the other layers, but ideally it shouldn't contain any
  domain or business logic. If we ever offer a different interface (GraphQL, CLI, etc), we would
  modify/duplicate this layer to adapt the application to a technology other than REST. Therefore,
  we want this layer focused on orchestrating/presenting and not much else.

For now, these packages are all in the same module, and architecture rules are enforced by
[ArchUnit](https://www.archunit.org/) just to help prevent accidents.

### Design principles and conventions

#### General

- API requests are always scoped to a single organization, and all stored data is scoped by org as well.
- All sync operations should be asynchronous, message-based, and orchestrated through Kafka.
- Optimize primarily for common, high-volume reads like checking availability.

#### Validation

- Treat core model validation as business rules, which means...
- Core model validation should happen in core services, not in the API layer.
- Use `javax.validation` annotations on models that need to be validated.
- Usually, the API layer should be able to receive a request and map it into something that can be
  passed into a service, all without error. Then, the service must determine if the request is valid
  and throw the appropriate exception.

#### Null handling

- Annotate model params with `@javax.validation.constraints.NotNull` to indicate non-nullable
  fields (avoid the many other variants of non-null annotations).
  - We decided to standardize on this instead of `@Nonnull` because it's really hard to not
    accidentally use the wrong one, and we need the validation variant for validation to work.
  - We have an ArchUnit test that should help prevent importing other non-null attributes.
  - You can add `javax.validation.constraints.NotNull` to the list of non-null annotations in your IDE
    to get intellisense and warnings.
- Use `@lombok.NonNull` sparingly and only for things that should never be null at runtime, such as
  id value objects and things that don't even make sense as null. This annotation adds a runtime
  null check, so adding it to DTOs and request objects means a `NullReferenceException` will occur
  before the objects can even be validated, which is usually undesirable.
- Don't pass around nulls, and don't return nulls. More specifically...
  - Don't create public constructors or methods that require us to pass null into them. In those cases,
    create method overloads, object builders, etc. so that we're not passing around nulls. We don't want
    to have to annotate every method param with `@Nonnull`, and other Java-world options are cumbersome.
    In the rare case you need to accept a null, mark the param with `@Nullable` for clarity.
  - Don't return nulls from methods. Use `Optional<T>` instead. In the rare cases where null needs to
    be returned, use the `@Nullable` annotation to clearly mark your crime.
- Use only the `@org.springframework.lang.Nullable` variant of `@Nullable`, and very sparingly.
  - There are lots of similar annotations in Java-world, so we're picking just one for consistency.
  - We have an ArchUnit test that should help prevent importing other, unapproved variants.

#### Code todos

We use a few types of TODO comments in code so that we can avoid having tons of TODOs that never
actually get done. When you look at the TODO list, it should contain only things that actually need
to be done soon. If you ignore the other TODO types, that's mostly fine too because the main point
is to keep the TODO list short and actionable. You can configure your IDE to filter/group the
additional TODO types as needed.

```
// TODO: This is something that actually needs to be done soon. Don't use this for hypothetical
// improvements or anything else that may never actually get done. These items should jump out at us
// so that we actually do them (and soon).

// DO-LATER: This is something we need to do, but maybe not soon. These items may live in the code
// for longer. There may be an associated ticket in the backlog, so maybe you don't need to mark it
// in code at all, except where there's something helpful to add.

// DO-MAYBE: This is something that may be a good idea, or not. No one will probably care about it
// unless they're working on the associated code, and then it may come in handy.
```

----------

## Getting started

### Requirements

- [Adoptium JDK 17 (Temurin)](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot)
- Docker
- IntelliJ IDEA (recommended but optional)

### Setup the db

Before running the app, you need to setup your local db.

1. Start the app dependencies: `docker-compose up -d`
2. Init the db (runs migrations and generates jooq classes): `./gradlew initDb`

### Run the app

Run the app:
```
./gradlew bootRun
```

You can pass project properties to `bootRun` to override the defaults.
For example:
```
./gradlew bootRun -Pprofile=local,me -Plog_appender=console_json
```

### Run/debug with IntelliJ

In addition to Gradle, you can also run/debug the app with IntelliJ:

1. Add a new Run/Debug configuration with the Spring Boot template.
2. Use these settings:
    - Main class: `com.UoU.app.Application`
    - Environment variables: `LOG_APPENDER=console`
    - Active profiles: `local,me`
    - JRE: `Adoptium JDK 17 (temurin-17)`
3. Press the Run or Debug button to start the app.

### Open and verify the app

You can load the API documentation page to ensure the app works:
http://localhost:2006/docs

### Create personal application profile (optional)

You can create `application-me.yml` in the main and/or test resources folders to set up an
application profile that is specific to you and will not be checked in.

The `./gradlew bootRun` task automatically loads the active profiles `local,me` so that `me` will
override `local`. Similarly, application integration tests automatically load the profiles `test,me`
so that your personal profile will override the base configuration if it exists. When running the
app through IntelliJ or manually specifying profiles, you'll want to set the profiles to `local,me`.

*This is completely optional, and you can still use environment variables or any other method of
configuring the app in addition to this method.*

See:

- [src/main/resources/application-me.yml.example](src/main/resources/application-me.yml.example)
- [src/test/resources/application-me.yml.example](src/test/resources/application-me.yml.example)

### See available Gradle tasks

We have a bunch of helpful Gradle tasks. See the list like this:

```
./gradlew tasks
```

----------

## Authentication and Authorization

The API is secured by JWT bearer tokens placed in the Authorization header:
```
Authorization: Bearer <jwt>
```

The `scope` claim will be used for authorizing access to specific resources.
See the API docs for details about claims and scopes.

We validate JWTs using RS256 JWK public/private keys. The production app only needs the JWK public
key to validate JWTs (no private key is necessary).


### Example JWT

Below is an example JWT that's suitable for local testing. It was generated with the example JWK
(below) and so can be validated with that JWK as well.

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwib3JnX2lkIjoidGVzdCIsImF1ZCI6ImNhbGVuZGFycyIsImlzcyI6ImZvci10ZXN0aW5nIiwiaWF0IjoxNjQ0NjEzNTMxLCJleHAiOjMyNTAzNzA1MjAwLCJzY29wZSI6ImFjY291bnRzIGNhbGVuZGFycyBldmVudHMgZGlhZ25vc3RpY3MgYWRtaW4ifQ.Erjx0eMY-dSUPfrZ-qejNkrdZ7VTtjABW4itkt8LIxv2LnMslingaHnQP5KsTcGF7oDB7dBM7lD0EqgLF9BcYdP62L1-e2I3pNX4PaloBkdjNgoSNEgRcPWuO3ecQdD5oo3T2Tlhw0YhN6JxSe-ipBs10CklK54crhBoupoN6vlfVz4AXFb7Y0fZ1QRrnHPmeHOT3kA8gpQvOtjqu17Zgy1570gVHSSxq7_bipFoMoSbJcOTLKe9pbgQh4x2YukxZqgcZCh2Grz81ad7Zm9bLTWch1yEd9WvyqFZzhxDKOSAZGz7eBNubngHvWYqywbPm2ASmA_jWNOD8q6aieJtcg
```
[Decode JWT](https://jwt.io/#debugger-io?token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwib3JnX2lkIjoidGVzdCIsImF1ZCI6ImNhbGVuZGFycyIsImlzcyI6ImZvci10ZXN0aW5nIiwiaWF0IjoxNjQ0NjEzNTMxLCJleHAiOjMyNTAzNzA1MjAwLCJzY29wZSI6ImFjY291bnRzIGNhbGVuZGFycyBldmVudHMgZGlhZ25vc3RpY3MgYWRtaW4ifQ.Erjx0eMY-dSUPfrZ-qejNkrdZ7VTtjABW4itkt8LIxv2LnMslingaHnQP5KsTcGF7oDB7dBM7lD0EqgLF9BcYdP62L1-e2I3pNX4PaloBkdjNgoSNEgRcPWuO3ecQdD5oo3T2Tlhw0YhN6JxSe-ipBs10CklK54crhBoupoN6vlfVz4AXFb7Y0fZ1QRrnHPmeHOT3kA8gpQvOtjqu17Zgy1570gVHSSxq7_bipFoMoSbJcOTLKe9pbgQh4x2YukxZqgcZCh2Grz81ad7Zm9bLTWch1yEd9WvyqFZzhxDKOSAZGz7eBNubngHvWYqywbPm2ASmA_jWNOD8q6aieJtcg)

### Example JWK

You can use this JWK to generate new JWTs for local testing, such as with [jwt.io](https://jwt.io).

```
// Public JWK (for validating JWTs):
{
  "kty": "RSA",
  "e": "AQAB",
  "n": "nkuCJYwc_n8hVigytGePfITCEBHCUpSvZjduzyViYjLkZ8a9_HDrJfxTrrAoQs4UOJuxlOd3Z1kxzxUdrgmJuf6QVsIf2Sck0Hl1tRdnJdX9hF3bnprg3imgt2o6-zdw7nBCw6_TD9UHybmOrV5biEW4ilw6z57RYGUy5QA6ludnd0ZoiSkIym1xIRPHy1HeQoyE7ecZCWDwRd6bkvpu25v3bzFxbgBVAvPI5DZuBaGCJOVqy_61S_Ol4S3HMqj_ZB1SIuvq-pusro82VL90ioEADUjQThNIFtqdAfse8Xy4kx20Ib7mH6reWdWQhzfgv9HPTpHc6STehty6kbacUQ"
}

// Full JWK (includes private key for creating/signing JWTs):
{
  "p": "yhTuqFiubvRBXjb4F0ksSJGvVsDEM8SEY1pJDOQsXWp22momboLLRO8xnt5i7uddMEx_FnH5iVGYQAZ87JIMR0Lk4CnVq_mTSmXTKF4pPHlqEHzUCs6n7kJfLlZIlNMNdaOtl429rgfeOwHoMk1wev5JPR6B8bZY9-tep0VXn5s",
  "kty": "RSA",
  "q": "yIe-2rK_kc0GzRsSgfy-YQzA2FBAVrzokFP3wcc8pMDJy2VyGduKXWullluUYBjpRlhlrsAjyuxC0LMayhfWQrXFnwoCiKHjS598COtNULJ-bTqUnVJ-GM-N5yBkcE60h45tCrF3mu1KqZpJTWfPCysPmiDIlpH-l8nckwS20IM",
  "d": "cDIOvfQwfMW46Cs3vRyF2_jw35jmxcdzBp0VoibaM8XklBWOhUW3tcXBvlhHQQp11KWt2V-yBwN2RVOvJLLH9peS0JtpHW4cLtNX9ZHv3yZsOQyZZDJcXbrdmgLmaTajiazx9WdCzr7sdTNbCFCPyee45AB5Ar2Lo6o2x49A98FQ-9tRFauiB3FuqE7WtWEwM32N8jB2W7A4gXIsUJmHkvw2v0sPLbaMSJFwrENvBknAveo3dfkD5IzR8yvu7jLT-ON61E-BfLB5_l5M9PGoZCjugFWPxEApTIfbAYbKzqyIsTsOyyVEy2MykxKdhWVlN6_oClRBjHPYgMbaeG-fXQ",
  "e": "AQAB",
  "qi": "ZTcD3ijlFr9Q3A7q5xQY1QVYli4A-Cl-v4IHDnYjvLJUUhzycPQJhag2XoyJm-rEomIbNTQzkl9ATC2i_FDSgmlMhfK8Bn72RhhJ3DTOHH61wBNH09eN9p77FdMaCH6UFMvcIbSn7bzQ5moccNysc83pYSbq2dHDhed6pAfpHbU",
  "dp": "qfR825zp52sJ5wD4Gi1yTv4npyCsYpVuelieg3cLUO2PVBZeiCb8add5thF2x1JSb6KGaAwnQDQdhgtu2U_jf7Nk5pqImABovfNxacv3hmLer0ss0oIHfjzX7BKpLHFsKnokLwgIdOstHhd2f4fdh7OBSSP1SaKApNGRY8DP9u0",
  "dq": "a-j4oHGeO1SMe2U9IWwj-s_FGuKcrNB9ieVVpxaTIMmKDgUqUJXesIUUF4RypP_i0HMTTOWinTlkfzBSkzwelcmBbiMR1_rd-Lz7H0WYVnunIxpqDPruVmWipCS_R9xis6vGLAHyLhHRotEe7yOUvBrRC4zmxHgN_BqpswfPZQc",
  "n": "nkuCJYwc_n8hVigytGePfITCEBHCUpSvZjduzyViYjLkZ8a9_HDrJfxTrrAoQs4UOJuxlOd3Z1kxzxUdrgmJuf6QVsIf2Sck0Hl1tRdnJdX9hF3bnprg3imgt2o6-zdw7nBCw6_TD9UHybmOrV5biEW4ilw6z57RYGUy5QA6ludnd0ZoiSkIym1xIRPHy1HeQoyE7ecZCWDwRd6bkvpu25v3bzFxbgBVAvPI5DZuBaGCJOVqy_61S_Ol4S3HMqj_ZB1SIuvq-pusro82VL90ioEADUjQThNIFtqdAfse8Xy4kx20Ib7mH6reWdWQhzfgv9HPTpHc6STehty6kbacUQ"
}
```

### Generate JWKs

If needed, you can create your own JWKs using one of these tools:

- [mkjwk simple JSON Web Key Generator](https://mkjwk.org/)
- [Local KeyGenerator](src/test/java/com/UoU/KeyGenerator.java)

## API documentation

We use springdoc-openapi to generate OpenAPI 3 docs and a swagger UI. We also maintain a static
HTML overview page in [src/main/resources/static/docs](src/main/resources/static/docs) because
it's much easier than including detailed information inside the swagger UI.

To see the docs, run the app and then open:
[http://localhost:2006/docs](http://localhost:2006/docs)

### Public and private groups

For local environments only, API endpoints are split into public and private groups. The private
group contains all the endpoints that should only be seen by developers of this API, such as
actuator endpoints. In non-local environments, there are no groups, and the private endpoints
are simply removed.

----------

## Database migrations

We use flyway for database migrations and generated Jooq classes for type-safe db access.
For initial setup, or whenever you add a migration, you need to run both `flywayMigrate`
and `generateJooq` to apply the migration and generate new jooq classes, respectively.

There is a gradle shortcut that runs the db tasks for you. You can run this when you first
clone the repo and whenever you need to add a new migration and apply it.
```
docker-compose up -d
./gradlew initDb
```

To wipe your db and start fresh:
```
docker-compose up -d
./gradlew flywayClean initDb # WARNING: will wipe your db
```

There is also a gradle helper to run both `initDb` and `build`:
```
./gradlew buildWithInitDb
```

### Local migrations

The [local migrations](local-migrations) folder contains migrations that run for local development
only. You can add useful seed data here so that devs don't have to start with an empty db.

----------

## Testing

Tests are divided into two types:

- Unit tests in the top-level [test dir](./src/test/java/com/UoU)
- Integrations tests in the [_integration dir](./src/test/java/com/UoU/_integration)

To run tests, use one of these gradle tasks:

```
./gradlew test # unit tests
./gradlew testIntegration # integration tests
./gradlew testAll # all tests
```

### Unit tests

Unit tests exercise small units of code. They should be fast and isolated from dependencies.
We should aim to have a lot more unit tests than integration tests.

Run the unit tests like this:

```
./gradlew test
```

### Integration tests

Integration tests require dependencies, I/O, etc. and may be slower than unit tests.
These tests should be placed in the [_integration dir](./src/test/java/com/UoU/_integration)
so we can run them separately.

We use [Testcontainers](https://www.testcontainers.org/) to run Docker test dependencies such as
a db container.

Run the integration tests like this:

```
./gradlew testIntegration
```

----------

## Infrastructure and deployment

This API is designed to run in containers. Specifically, we use Kubernetes and some other tools to
help us deploy containers on Kubernetes:

- CI/CD: [CodeFresh](https://g.codefresh.io/pipelines/edit/new/builds?id=6234dfb2c718107ccc1723fb&pipeline=calendars&projects=UoU&projectId=61b147ded8e622706794c8a3&filter=page:1)
- Argo Workflows (for Kubernetes tasks): [Argo](https://argo.qa.UoUconnect.com)
- Kubernetes
  - Config (helm chart): [kubernetes-config](https://github.com/UoU/kubernetes-config/tree/main/helm/charts/calendars)
  - Releases: [kubernetes-releases](https://github.com/UoU/kubernetes-releases)


## Nylas tips and tricks

### Nylas CLI for webhooks

When developing locally, you can use the [Nylas CLI](https://developer.nylas.com/docs/developer-tools/cli/)
to easily create webhook subscriptions with a tunnel to localhost. This has the same effect as
creating the webhook subscription yourself and then using ngrok (or similar) to create a tunnel,
except the CLI automatically creates and destroys the webhook subscription for you.

```console
brew install nylas/nylas-cli/nylas
nylas init
nylas webhook tunnel -f http://localhost:2006/v1/inbound-webhooks/nylas \
    -t account.connected,account.running,account.stopped,account.invalid,account.sync_error,calendar.created,calendar.updated,calendar.deleted,event.created,event.updated,event.deleted
```

Now, when you `Ctrl+C` to close the tunnel, the webhook subscription will be deleted automatically.
