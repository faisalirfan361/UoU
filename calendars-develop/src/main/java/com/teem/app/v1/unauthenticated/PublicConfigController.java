package com.UoU.app.v1.unauthenticated;

import com.UoU.app.v1.dtos.PublicConfigDto;
import com.UoU.core.events.EventsConfig;
import com.UoU.infra.kafka.TopicNames;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes public config values, primarily for use in docs to tell users about dynamic config.
 *
 * <p>IMPORTANT: Don't expose anything that's not suitable for public, unauthenticated callers.
 */
@RestController
@RequestMapping("/v1/config")
@AllArgsConstructor
@Tag(
    name = "Public config",
    description = "Config that is suitable for public places like docs")
public class PublicConfigController {
  private static final String AVRO_CONTENT_TYPE = "application/vnd.kafka.avro.v2+json";

  private final EventsConfig eventsConfig;
  private final TopicNames.PublicEvents publicEventTopicNames;

  @GetMapping
  @SneakyThrows
  public PublicConfigDto get() {
    return new PublicConfigDto(
        eventsConfig.activePeriod(),
        Map.of(
            "event-changed",
            Pair.of(publicEventTopicNames.getEventChanged(), "/v1/config/schemas/event-changed")
        ));
  }

  /**
   * Returns PUBLIC EventChanged avro schema for docs since it's part of the public contract.
   */
  @GetMapping(value = "/schemas/event-changed", produces = AVRO_CONTENT_TYPE)
  public String getPublicAvroEventChanged() {
    return com.UoU.infra.avro.publicevents.EventChanged.getClassSchema().toString(true);
  }
}
