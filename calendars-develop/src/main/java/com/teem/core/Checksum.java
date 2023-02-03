package com.UoU.core;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.CRC32C;
import lombok.Getter;
import lombok.val;

/**
 * Helper for computing CRC32C checksums, which are fast but NOT SECURE.
 *
 * <p>Use where speed matters, for sanity checking data, NOT WHERE SECURITY MATTERS!
 */
@Getter
public class Checksum {
  private static final String INDEX_WRAP_OPEN = "(";
  private static final String INDEX_WRAP_CLOSE = ")";

  private final String value;

  public Checksum(String... values) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Values cannot be empty");
    }

    val bytes = IntStream
        .range(0, values.length)
        // Wrap w/ index like 0(value) to reduce chance of collisions:
        .mapToObj(i -> i + INDEX_WRAP_OPEN + values[i] + INDEX_WRAP_CLOSE)
        .collect(Collectors.joining())
        .getBytes(StandardCharsets.UTF_8);

    val checksum = new CRC32C();
    checksum.update(bytes, 0, bytes.length);

    // Add values.length and bytes.length to reduce chance of collisions.
    value = String.valueOf(checksum.getValue()) + values.length + bytes.length;
  }
}
