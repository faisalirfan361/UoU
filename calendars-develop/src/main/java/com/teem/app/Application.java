package com.UoU.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan("com.UoU")
@ConfigurationPropertiesScan("com.UoU")
@EnableScheduling
@Slf4j
public class Application {

  private static final String ENV_BUFFERING_APP_STARTUP = "BUFFERING_APP_STARTUP";

  public static void main(String[] args) {
    val app = new SpringApplication(Application.class);
    val warnings = new ArrayList<String>();

    // Check if ENV_BUFFERING_APP_STARTUP is on for troubleshooting startup issues.
    if (Optional
        .ofNullable(System.getenv(ENV_BUFFERING_APP_STARTUP))
        .filter(x -> "true".equals(x) || "1".equals(x))
        .isPresent()) {
      app.setApplicationStartup(new BufferingApplicationStartup(2000));
      warnings.add(ENV_BUFFERING_APP_STARTUP
          + " is on for extra startup metrics at /actuator/startup. DON'T LEAVE THIS ON!");
    }

    app.run(args);
    warnings.forEach(log::warn);
  }

  /**
   * General purpose rest template with short(ish) timeouts for external calls.
   *
   * <p>If you need specific timeouts, define a new rest template with whatever you need.
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
        .setConnectTimeout(Duration.ofSeconds(15))
        .setReadTimeout(Duration.ofSeconds(15))
        .build();
  }
}
