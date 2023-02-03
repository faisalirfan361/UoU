package com.UoU.core.calendars;

import com.UoU.core.DataConfig;
import java.time.ZoneId;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;

/**
 * ZoneId supplier that gets a string id from an inner supplier and parses it or returns a default.
 */
@AllArgsConstructor
public class ZoneParserSupplier implements Supplier<ZoneId> {
  private final Supplier<String> valueSupplier;
  private final boolean useDefaultOnFailure;

  @Override
  public ZoneId get() {
    try {
      return ZoneId.of(valueSupplier.get());
    } catch (Exception ex) {
      if (useDefaultOnFailure) {
        return DataConfig.Calendars.DEFAULT_TIMEZONE;
      }
      throw ex;
    }
  }
}
