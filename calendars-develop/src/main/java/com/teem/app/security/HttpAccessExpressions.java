package com.UoU.app.security;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for some HTTP paths that allow Spring EL expressions to configure access.
 *
 * <p>Most expressions will only need: hasIpAddress(), denyAll, permitAll.
 *
 * <p><a href="https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/el-access.html">See Spring Docs.</a>
 */
@ConfigurationProperties(prefix = "http-access-expressions")
record HttpAccessExpressions(
    String health,
    String actuator
) {

  private static final String DEFAULT_EXPRESSION = "denyAll";

  HttpAccessExpressions(String health, String actuator) {
    this.health = create(health);
    this.actuator = create(actuator);
  }

  private static String create(String expression) {
    return Optional.ofNullable(expression).filter(x -> !x.isBlank()).orElse(DEFAULT_EXPRESSION);
  }
}
