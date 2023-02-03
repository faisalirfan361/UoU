package com.UoU.infra.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

/**
 * Customizer for building ObjectMappers, which spring boot will use in addition to defaults.
 *
 * <p>Put custom config here instead of application.yml so it's used even if spring doesn't
 * create the ObjectMapper itself and so all the config is one place.
 *
 * <p>Try to keep mostly defaults, but re-set some spring things so spring boot doesn't necessarily
 * have to create the mapper. For example, these are defaults:
 * - WRITE_DATES_AS_TIMESTAMPS=false
 * - WRITE_DURATIONS_AS_TIMESTAMPS=false
 * - PropertyNamingStrategies.LOWER_CAMEL_CASE
 */
@Component
public class ObjectMapperBuilderCustomizer implements Jackson2ObjectMapperBuilderCustomizer {

  @Override
  public void customize(Jackson2ObjectMapperBuilder builder) {
    builder
        .modulesToInstall(new RecordNamingStrategyPatchModule())
        .featuresToDisable(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
        .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
  }
}
