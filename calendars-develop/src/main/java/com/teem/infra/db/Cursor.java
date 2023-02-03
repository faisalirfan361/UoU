package com.UoU.infra.db;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.function.TriFunction;

/**
 * Pagination cursor for encoding and decoding URL-safe cursor strings.
 *
 * <p>Encoded cursor strings are opaque, URL-safe strings that can be passed back to whatever
 * generated the cursor to fetch the next page. Therefore, only this class that encodes the cursors
 * needs to know how to decode them.
 *
 * <p>This lives in the db layer because these cursors only have meaning to the db code. To the rest
 * of the app, the cursors are opaque strings that get passed around without interpretation.
 *
 * <p>All cursors are base64 (URL-safe) encoded. However, this is not sufficient for cursors that
 * contain PII (like emails) because the cursors may be passed around and logged. FOR PII, you
 * must encrypt the cursors as well! Another byte encoder/decoder can be passed to the encode/decode
 * methods for encryption (or any other transformations).
 */
record Cursor(@NotNull List<String> values) {

  /**
   * Delimiter for joining values into a single string.
   *
   * <p>This needs to be something that's very unlikely to occur in cursor values. Most values are
   * ids and dates, but we want this to be safe for titles, names, etc. as well.
   *
   * <p>Since this will also be used to String.split(), and java will treat it as a regex, it needs
   * to avoid any special regex chars.
   */
  private static final String DELIMITER = "`~>,<~`";

  /**
   * Creates a cursor with an array objects that will have toString() called on them.
   */
  public Cursor(Object... values) {
    this(Arrays.stream(values).map(x -> x == null ? null : x.toString()).toList());
  }

  public Cursor {
    if (values == null || values.isEmpty() || values.stream().anyMatch(x -> x == null)) {
      throw new IllegalArgumentException("Cursor must have non-null values");
    }
  }

  public String value(int index) {
    return values.get(index);
  }

  /**
   * Encodes the cursor values to a URL-safe string.
   */
  public String encode() {
    return encode(str -> str.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Encodes the cursor values to a URL-safe string with additional transformation by a byteEncoder.
   *
   * <p>The byteEncoder could encrypt the input string or do any other transformation that needs to
   * happen before final transformation into a URL-safe string.
   */
  public String encode(Function<String, byte[]> byteEncoder) {
    return base64Encode(byteEncoder.apply(String.join(DELIMITER, values)));
  }

  private static String base64Encode(byte[] value) {
    var encoded = Base64.getUrlEncoder().encode(value);
    return new String(encoded, StandardCharsets.UTF_8);
  }

  /**
   * Gets a decoder to convert a previously encoded string back into values.
   */
  public static Decoder decoder() {
    return new Decoder(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Gets a decoder like {@link #decoder()} but also does additional transformation via byteDecoder.
   *
   * <p>This should be used in conjunction with {@link #encode(Function)} to reverse whatever
   * transformations were done when the cursor was encoded. Typically, this is used for encryption,
   * where the byteEncoder encrypts and the byteDecoder decrypts.
   */
  public static Decoder decoder(Function<byte[], String> byteDecoder) {
    return new Decoder(Optional.ofNullable(byteDecoder).orElseThrow());
  }

  /**
   * Decodes cursor strings that have been encoded via {@link Cursor}.
   */
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Decoder {
    private final Function<byte[], String> byteDecoder;

    /**
     * Decodes a cursor with the expected valueCount number of values.
     */
    public Optional<Cursor> decode(String encoded, int valueCount) {
      return Optional
          .ofNullable(encoded)
          .filter(x -> !x.isBlank())
          .map(x -> {
            try {
              var decoded = Base64.getUrlDecoder().decode(x);
              return byteDecoder.apply(decoded);
            } catch (Exception ex) {
              throw new InvalidCursorException(ex);
            }
          })
          .map(x -> {
            var items = x.split(DELIMITER);
            if (items.length != valueCount) {
              throw new InvalidCursorException();
            }
            return items;
          })
          .map(Cursor::new);
    }

    /**
     * Decodes a cursor and allows you to map the values to something else.
     *
     * <p>If the mapper throws an error, this will throw a {@link InvalidCursorException}, so it's
     * best to apply any mapping logic within the mapper passed to this method.
     */
    public <T> Optional<T> decodeAndMap(
        String encoded, int valueCount, Function<Cursor, T> mapper) {
      return decode(encoded, valueCount)
          .map(x -> {
            try {
              return mapper.apply(x);
            } catch (Exception ex) {
              throw new InvalidCursorException(ex);
            }
          });
    }

    /**
     * Shortcut for {@link #decodeAndMap} for a single value.
     */
    public <T> Optional<T> decodeOneAndMap(String encoded, Function<String, T> mapper) {
      return decodeAndMap(encoded, 1, x -> mapper.apply(x.value(0)));
    }

    /**
     * Shortcut for {@link #decodeAndMap} for a single string value that should be returned as-is.
     */
    public Optional<String> decodeOneToString(String encoded) {
      return decodeOneAndMap(encoded, x -> x);
    }

    /**
     * Shortcut for {@link #decodeAndMap} for two values.
     */
    public <T> Optional<T> decodeTwoAndMap(
        String encoded, BiFunction<String, String, T> mapper) {
      return decodeAndMap(encoded, 2, x -> mapper.apply(x.value(0), x.value(1)));
    }

    /**
     * Shortcut for {@link #decodeAndMap} for three values.
     */
    public <T> Optional<T> decodeThreeAndMap(
        String encoded, TriFunction<String, String, String, T> mapper) {
      return decodeAndMap(encoded, 3, x -> mapper.apply(x.value(0), x.value(1), x.value(2)));
    }
  }
}
