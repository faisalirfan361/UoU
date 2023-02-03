package com.UoU.infra.db;

import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * Helper to lazily fetch a batch of calendar timezone strings and convert to ZoneIds.
 *
 * <p>Since the mapSupplier is lazy, this helps you not fetch all the timezones unless they're
 * needed. When any timezone is needed, they can all be supplied at once via a single query.
 *
 * <p>IMPORTANT: This is NOT thread-safe. It's only intended to be used for a single operation.
 */
@RequiredArgsConstructor
class LazyBatchCalendarTimeZoneSupplier<T> {
  private final Supplier<Map<T, String>> mapSupplier;
  private Map<T, String> map;

  public Supplier<ZoneId> createSupplier(T key) {
    return () -> Optional
        .ofNullable(fetchMap().get(key))
        .map(name -> {
          try {
            return ZoneId.of(name);
          } catch (Exception ex) {
            // Hopefully rare, but somehow the db has an invalid timezone:
            throw new IllegalStateException("Calendar has an invalid timezone: " + key, ex);
          }
        })
        .orElseThrow(() -> new IllegalStateException("Calendar is missing timezone: " + key));
  }

  private Map<T, String> fetchMap() {
    if (map == null) {
      map = mapSupplier.get();
    }
    return map;
  }
}
