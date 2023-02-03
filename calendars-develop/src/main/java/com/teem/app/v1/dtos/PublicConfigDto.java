package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import com.UoU.core.events.EventsConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Public config values, primarily for use in docs to tell users about dynamic config.
 *
 * <p>IMPORTANT: Don't expose anything that's not suitable for public, unauthenticated callers.
 */
@Schema(name = "PublicConfig", requiredProperties = SchemaExt.Required.ALL)
@Getter
public class PublicConfigDto {
  private final Map<String, Object> events;
  private final Map<String, KafkaEventConfig> kafka;

  public PublicConfigDto(
      EventsConfig.ActivePeriod eventsActivePeriod,
      Map<String, Pair<String, String>> kafkaTopicsAndSchemaUrls) {

    events = Map.of("activePeriod", eventsActivePeriod);
    kafka = kafkaTopicsAndSchemaUrls.entrySet().stream().collect(Collectors.toMap(
        x -> x.getKey(),
        x -> new KafkaEventConfig(x.getValue().getLeft(), x.getValue().getRight())));
  }

  private record KafkaEventConfig(String topic, String schemaUrl) {
  }
}
