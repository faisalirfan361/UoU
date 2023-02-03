package com.UoU._integration;

import com.UoU._helpers.TestData;
import com.UoU._integration._helpers.DbHelper;
import com.UoU._integration._helpers.RedisHelper;
import com.UoU.app.Application;
import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

/**
 * Base integration test that loads the spring boot application.
 *
 * <p>If you don't need to load spring boot, don't use this test!
 *
 * <p>This will load two application profiles:
 * - test (application-test.yml)
 * - me (application-me.yml)
 *
 * <p>You can put any user-specific values you want to use for tests in application-me.yml.
 */
@SpringBootTest(
    classes = {Application.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles(profiles = {"test", "me"})
public abstract class BaseAppIntegrationTest {

  protected final OrgId orgId = TestData.orgId();

  @Autowired
  protected DbHelper dbHelper;

  @Autowired
  protected RedisHelper redisHelper;

  /**
   * Creates and starts a new DB container for use by integration tests.
   *
   * <p>The username and password must match the flyway credentials that have been configured in
   * the app settings or the startup will fail.
   */
  @Container
  private static PostgreSQLContainer dbContainer = Fluent
      .of(new PostgreSQLContainer(DockerImageName.parse("postgres:13.6-alpine")))
      .also(x -> x
          .withDatabaseName("calendars")
          .withUsername("postgres")
          .withPassword("postgres")
          .start())
      .get();

  @Container
  public static GenericContainer redisContainer = Fluent
      .of(new GenericContainer(DockerImageName.parse("redis:6.0-alpine")))
      .also(x -> x
          .withExposedPorts(6379)
          .start())
      .get();

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", dbContainer::getJdbcUrl);
    registry.add("spring.datasource.username", dbContainer::getUsername);
    registry.add("spring.datasource.password", dbContainer::getPassword);
    registry.add("spring.redis.url", () -> String.format(
        "redis://%s:%d/0", redisContainer.getHost(), redisContainer.getFirstMappedPort()));
  }
}
