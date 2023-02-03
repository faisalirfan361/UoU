plugins {
  java
  id("org.springframework.boot") version "2.7.3"
  id("io.spring.dependency-management") version "1.0.14.RELEASE"
  id("org.flywaydb.flyway") version "9.3.1"
  id("nu.studer.jooq") version "6.0.1"
  id("com.github.davidmc24.gradle.plugin.avro") version "1.3.0"
  checkstyle
  jacoco
  id("io.qameta.allure") version "2.10.0"
}

group = "com.UoU"
version = "0.0.1-SNAPSHOT"
springBoot.mainClass.set("com.UoU.app.Application")

//// ------------
//// Java compilation config:
java.sourceCompatibility = JavaVersion.VERSION_17

tasks.withType<JavaCompile> {
  options.compilerArgs.add("-Xlint:unchecked")
  options.isDeprecation = true
}

tasks.jar {
  exclude("*.example", "*.md")
}

// ------------
// Dependencies
configurations {
  all {
    // Exclude default logging starter with Logback so we can use spring-boot-starter-log4j2:
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
  }

  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

ext {
  set("springBootAdminVersion", "2.7.3")
  set("testcontainersVersion", "1.17.3")
  set("rest-assured.version", "5.1.1")
}
val allureVersion = "2.13.9"

repositories {
  mavenCentral()
  maven {
    setUrl("https://packages.confluent.io/maven/")
  }
  maven {
    setUrl("https://jitpack.io") // required for confluent transitive dependencies
  }
  gradlePluginPortal() // for avro plugin
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  // Database
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  runtimeOnly("org.postgresql:postgresql")
  jooqGenerator("org.postgresql:postgresql")
  implementation("org.flywaydb:flyway-core")

  // Redis
  implementation("org.springframework.boot:spring-boot-starter-data-redis")

  // Kafka
  // TODO: Handle retry/backoff errors so they're not logged as errors, which should be
  // fixed in the next version of spring-kafka, 2.8.5. See https://stackoverflow.com/q/71719065
  val confluentPlatformVersion = "7.1.2"
  implementation("org.springframework.kafka:spring-kafka")
  implementation("org.apache.avro:avro:1.11.0")
  implementation("io.confluent:kafka-avro-serializer:$confluentPlatformVersion")
  implementation("io.confluent:kafka-schema-registry:$confluentPlatformVersion") {
    // Exclude older log4j binding that will produce a duplicate binding warning:
    exclude(group = "org.slf4j", module = "slf4j-log4j12")
  }
  implementation("io.confluent:kafka-schema-registry-client:$confluentPlatformVersion")

  // Object mapping, validation, and other helpers
  val mapstructVersion = "1.5.2.Final"
  implementation("org.mapstruct:mapstruct:$mapstructVersion")
  annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
  implementation("org.apache.commons:commons-lang3")
  implementation("org.dmfs:lib-recur:0.12.2") // rrule validation

  // Resilience4j (circuit breakers, retry, etc.)
  // DO-LATER: Add resilience4j when we need it to implement circuit breakers and things.
  // val resilience4jVersion = "1.7.1"
  // implementation("io.github.resilience4j:resilience4j-spring-boot2:$resilience4jVersion")
  // runtimeOnly("org.springframework.boot:spring-boot-starter-aop")

  // Scheduled tasks
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.41.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:4.41.0")

  // Observability, logging, and metrics
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("io.micrometer:micrometer-registry-datadog")
  implementation("de.codecentric:spring-boot-admin-starter-client")
  runtimeOnly("org.springframework.boot:spring-boot-starter-log4j2")
  runtimeOnly("com.lmax:disruptor:3.4.4") // needed for log4j2 async loggers

  // 3rd-party API SDKs
  implementation("com.nylas.sdk:nylas-java-sdk:1.16.0")
  implementation("com.microsoft.graph:microsoft-graph:5.42.0") // For MS Teams meetings

  // Development and testing
  // DO-LATER: devtools is causing cast exceptions when trying to consume kafka messages.
  // This is an old issue with the avro classloader that it seems they won't fix, so we'll probably
  // need to implement a workaround ourselves eventually. See:
  // - https://issues.apache.org/jira/browse/AVRO-1425
  // - https://github.com/spring-projects/spring-boot/issues/14622
  // developmentOnly("org.springframework.boot:spring-boot-devtools")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
  testAnnotationProcessor("org.projectlombok:lombok")
  testCompileOnly("org.projectlombok:lombok")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    // Remove duplicate org.json.JSONObject dependency warning from test runs:
    exclude(group = "com.vaadin.external.google", module = "android-json")
  }
  testImplementation("com.tngtech.archunit:archunit-junit5:1.0.0-rc1")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("io.rest-assured:rest-assured")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0") // for ms-graph/okhttp testing

  // API documentation
  implementation("org.springdoc:springdoc-openapi-ui:1.6.11")

  // Template engine
  implementation("org.springframework.boot:spring-boot-starter-mustache")
}

dependencyManagement {
  imports {
    mavenBom("de.codecentric:spring-boot-admin-dependencies:${ext.get("springBootAdminVersion")}")
    mavenBom("org.testcontainers:testcontainers-bom:${ext.get("testcontainersVersion")}")
  }
}

// ------------
// DB, flyway, jooq code generation config.
// See https://www.jooq.org/doc/3.14/manual-single-page/#code-generation
flyway {
  url = System.getenv("FLYWAY_URL") ?: "jdbc:postgresql://localhost:5433/calendars"
  user = System.getenv("FLYWAY_USER") ?: "postgres"
  password = System.getenv("FLYWAY_PASSWORD") ?: "postgres"
  cleanDisabled = false
  locations = System.getenv("FLYWAY_LOCATIONS")?.split(",")?.toTypedArray()
      ?: arrayOf(
        "filesystem:src/main/resources/db/migration", // from gradle, app may not be built yet, so load from filesystem
        "classpath:db/migration", // but also load from classpath as per usual in case app is built
        "filesystem:local-migrations",
      )
}

jooq {
  version.set(dependencyManagement.importedProperties["jooq.version"]) // match spring boot version

  configurations {
    create("main") {
      jooqConfiguration.apply {
        generateSchemaSourceOnCompilation.set(false)
        logging = org.jooq.meta.jaxb.Logging.WARN
        jdbc.apply {
          driver = "org.postgresql.Driver"
          url = flyway.url
          user = flyway.user
          password = flyway.password
        }
        generator.apply {
          name = "org.jooq.codegen.DefaultGenerator"
          database.apply {
            name = "org.jooq.meta.postgres.PostgresDatabase"
            inputSchema = "public"
            includes = ".*"
            excludes = "^flyway_.*"
          }
          generate.apply {
            isDeprecated = false
            isRecords = true
            isDaos = false
            isPojos = false
            isFluentSetters = true
          }
          target.apply {
            packageName = "com.UoU.infra.jooq"
          }
          strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
        }
      }
    }
  }
}

buildscript {
  // Configure jooq plugin to use correct jooq version that matches spring boot managed jooq version.
  // See https://github.com/etiennestuder/gradle-jooq-plugin#configuring-the-jooq-generation-tool
  // and https://docs.spring.io/spring-boot/docs/current/reference/html/dependency-versions.html
  val jooqVersion = "3.14.16"
  configurations["classpath"].resolutionStrategy.eachDependency {
    if (requested.group == "org.jooq") {
      useVersion(jooqVersion)
    }
  }
}

tasks.withType<nu.studer.gradle.jooq.JooqGenerate> {
  inputs.files(fileTree("src/main/resources/db/migration"))
    .withPropertyName("migrations")
    .withPathSensitivity(PathSensitivity.RELATIVE)

  // Make jooq use incremental builds and caching.
  allInputsDeclared.set(true)
  outputs.cacheIf { true }

  // If running along with migrations, run migrations first.
  mustRunAfter(tasks.flywayMigrate)
}

tasks.compileJava {
  mustRunAfter(tasks.named("generateJooq"))
}

// ------------
// bootRun config:
// Setup to be suitable for local dev by default, since that's its main use.
tasks.bootRun {
  systemProperty(
    "spring.profiles.active",
    findProperty("profile") ?: System.getenv("SPRING_PROFILES_ACTIVE") ?: "local,me")
  environment(
    "LOG_APPENDER",
    findProperty("log_appender") ?: System.getenv("LOG_APPENDER") ?: "console")
}

// ------------
// Checkstyle config:
// - Use google_checks.xml included with checkstyle as our base checks.
// - But use our own checkstyle-suppressions.xml to suppress a few dumb rules.
// - Fail checkstyle tasks on errors rather than just report the warnings.
// - Ignore generated classes from jooq, avro, etc.
checkstyle {
  toolVersion = "10.3.1"
  val archive = configurations.checkstyle.get().resolve().filter {
    it.name.startsWith("checkstyle")
  }
  config = resources.text.fromArchiveEntry(archive, "google_checks.xml")
  System.setProperty(
    "org.checkstyle.google.suppressionfilter.config",
    "${project.projectDir}/checkstyle-suppressions.xml")

  isIgnoreFailures = false
  maxWarnings = 0

  tasks.withType<Checkstyle> {
    exclude("com/UoU/infra/avro/**")
    exclude("com/UoU/infra/jooq/**")
  }
}

// ------------
// Avro generation config:
// See https://github.com/davidmc24/gradle-avro-plugin#configuration
// - Make nullable fields return Optional.
avro {
  setGettersReturnOptional(true)
  setOptionalGettersForNullableFieldsOnly(true)
}

springBoot {
  buildInfo {
    properties {
      artifact = ""
      group = ""
      version = System.getenv("BUILD_INFO_VERSION") ?: "unknown"
    }
  }
}

// ------------
// Testing config with JUnit:
// Break tests into unit tests (test task) and integration tests (testIntegration task).
val integrationTestPattern = "com/UoU/_integration/**"
tasks.test {
  description = "Run unit tests (excludes integration tests)"
  exclude(integrationTestPattern)
  useJUnitPlatform()
}

tasks.register<Test>("testIntegration") {
  group = "verification"
  description = "Run integration tests only"
  include(integrationTestPattern)
  useJUnitPlatform()
}

tasks.register<Test>("testAll") {
  group = "verification"
  description = "Run all unit and integration tests"
  dependsOn(tasks.test, tasks.named("testIntegration"))
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report
  mustRunAfter(tasks.named("testIntegration")) // not a dependency, but do reports last
}

// Use allure for test reports because it works easily with CodeFresh.
allure {
  version.set(allureVersion)
  adapter {
    allureJavaVersion.set(allureVersion)
    autoconfigure.set(true)
    frameworks.junit5.enabled.set(true)
  }
}

tasks.jacocoTestCoverageVerification {
  violationRules {
    // For now, we're not going to require any specific coverage number, but we'll show a breakdown
    // by package with a 75% coverage goal to quickly spot packages with low coverage on demand.
    rule {
      element = "PACKAGE"
      excludes = listOf(
        "com.UoU.infra.avro.*",
        "com.UoU.infra.jooq.*",
      )

      limit {
        counter = "LINE"
        minimum = BigDecimal.valueOf(0.75)
      }
    }
  }
}

// ------------
// Other helper tasks
tasks.register("initDb") {
  group = "db"
  description = "Runs flywayMigrate and generateJooq so regular build will have db dependencies"
  dependsOn(tasks.flywayMigrate, tasks.named("generateJooq"))
}

tasks.register("buildWithInitDb") {
  group = "build"
  description = "Runs initDb and then build, which is useful for a full build after a clean"
  dependsOn(tasks.named("initDb"), tasks.build)
}

tasks.register("ci") {
  group = "verification"
  description = "Runs full build, check, and test for CI"
  dependsOn(tasks.build, tasks.check, tasks.named("testAll"), tasks.named("jacocoTestReport"))
}

tasks.register("flywayReset") {
  group = "flyway"
  description = "Resets the database by running flywayClean and then flywayMigrate"
  dependsOn(tasks.flywayClean, tasks.flywayMigrate)
}

tasks.flywayMigrate {
  mustRunAfter(tasks.flywayClean)
}
