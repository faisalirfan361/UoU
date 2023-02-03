package com.UoU.core.events;

import com.UoU.core.WrappedValue;
import java.util.Optional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Event data source: where the event data comes from, like an app or platform name.
 *
 * <p>Arbitrary data sources set by users from the API should be created via
 * {@link #fromApi(String)} so that they all have the same "api:" prefix. This ensures sources like
 * "provider" can't be set via the API, which would be wrong and confusing.
 */
public record DataSource(
    @NotBlank(message = "Data source cannot be empty.")
    @Size(
        min = 1,
        max = MAX_LENGTH,
        message = "Data source cannot be more than " + MAX_LENGTH + " chars, including the "
            + "prefix '" + API_PREFIX + "' that is automatically added for API requests.")
    String value
) implements WrappedValue<String> {
  private static final String API_PREFIX = "api:";
  private static final DataSource API_DEFAULT = new DataSource("api");

  static final int MAX_LENGTH = 50;
  static final int MAX_LENGTH_API = 46; // 50 - 4 chars for API_PREFIX

  // Known data sources to be used in our own code for consistency:
  public static final DataSource PROVIDER = new DataSource("provider");

  /**
   * Creates a data source from API user input, with a common prefix for consistency.
   *
   * <p>If the input is non-empty, it will be prefixed with "api:". Otherwise, a default of "api"
   * will be used.
   */
  public static DataSource fromApi(String name) {
    return Optional
        .ofNullable(name)
        .map(x -> x.strip())
        .filter(x -> !x.isBlank())
        .map(x -> new DataSource(API_PREFIX + x))
        .orElse(API_DEFAULT);
  }
}
