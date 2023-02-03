package com.UoU.app.docs;

import io.swagger.v3.oas.models.media.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * Custom extensions to @Schema to help configure DTOs and schemas more easily.
 */
public class SchemaExt {

  /**
   * Extensions for configuring required schema properties.
   */
  public static class Required {

    /**
     * Requires all properties in the schema, so you don't have to list or annotate each one.
     *
     * <p>`ALL` must be set as the first and only value for @Schema requiredProperties. Setting
     * other requiredProperties after `ALL` will have no effect.
     *
     * <p>Example:
     * <pre>{@code
     * @Schema(requiredProperties = SchemaExt.Required.ALL)
     * }</pre>
     */
    public static final String ALL = "#all";

    /**
     * Requires all properties in the schema, except specific named properties.
     *
     * <p>`EXCEPT` must be set as the first value for @Schema requiredProperties, and subsequent
     * values must be real property names that should be made optional. In other words, only the
     * specifically listed properties will be optional, and all other properties will be required.
     *
     * <p>Example:
     * <pre>{@code
     * @Schema(requiredProperties = {SchemaExt.Required.EXCEPT, "optional1", "optional2"})
     * }</pre>
     */
    public static final String EXCEPT = "#except";
  }

  /**
   * Processor to run during OpenAPI configuration to handle our custom Schema logic.
   */
  @Component
  protected static class Processor {
    public void process(Map<String, Schema> schemas) {
      schemas.values().forEach(this::processRequired);
    }

    private void processRequired(Schema<?> schema) {
      val required = Optional.ofNullable(schema.getRequired()).orElse(List.of());
      if (required.isEmpty()) {
        return;
      }

      val spec = required.get(0);

      if (spec.equals(Required.ALL)) {
        schema.setRequired(schema.getProperties().keySet().stream().toList());
      } else if (spec.equals(Required.EXCEPT)) {
        val newRequired = new HashSet<>(schema.getProperties().keySet());
        required.stream().skip(1).forEach(newRequired::remove);
        schema.setRequired(newRequired.stream().toList());
      }
    }
  }
}
