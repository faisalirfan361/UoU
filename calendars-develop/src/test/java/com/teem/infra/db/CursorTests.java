package com.UoU.infra.db;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class CursorTests {

  @Test
  void ctor_shouldRequireNonNullValues() {
    var exception = IllegalArgumentException.class;
    assertThatCode(() -> new Cursor()).isInstanceOf(exception);
    assertThatCode(() -> new Cursor((List<String>) null)).isInstanceOf(exception);
    assertThatCode(() -> new Cursor(List.of())).isInstanceOf(exception);
    assertThatCode(() -> new Cursor(null, "test")).isInstanceOf(exception);
  }

  @Test
  void encode_shouldBase64EncodeUrlSafeValues() {
    var values = List.of("value1", "value2");
    var encoded = new Cursor(values).encode();

    var decoded = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    assertThat(decoded).contains(values.get(0));
    assertThat(decoded).contains(values.get(1));
  }

  @Test
  void encode_decode_shouldRoundTrip() {
    var values = List.of("value1", "value2");
    var encoded = new Cursor(values).encode();
    var decoded = Cursor.decoder().decode(encoded, 2).orElseThrow();

    assertThat(decoded.values()).isEqualTo(values);
  }

  @Test
  void encode_decode_withByteEncoderAndDecoder_shouldRoundTrip() {
    var values = List.of("value1", "value2");
    var encoded = new Cursor(values).encode(FakeEncryptor::encrypt);
    var decoded = Cursor.decoder(FakeEncryptor::decrypt).decode(encoded, 2).orElseThrow();

    assertThat(decoded.values()).isEqualTo(values);
    assertThat(Cursor.decoder().decode(encoded, 2).orElseThrow().values()).isNotEqualTo(values);
  }

  @Test
  void decode_shouldThrowForInvalidBase64() {
    var values = List.of("value");
    var encoded = "not-base64-at-all";

    assertThatCode(() -> Cursor.decoder().decode(encoded, values.size()))
        .isInstanceOf(InvalidCursorException.class);
  }

  @Test
  void decode_shouldThrowForWrongValueCount() {
    var values = List.of("value");
    var encoded = new Cursor(values).encode();

    assertThatCode(() -> Cursor.decoder().decode(encoded, values.size() + 1))
        .isInstanceOf(InvalidCursorException.class);
  }

  @Test
  void decodeAndMap_shouldWork() {
    var values = List.of("5");
    var encoded = new Cursor(values).encode();

    var result = Cursor.decoder().decodeAndMap(
        encoded,
        values.size(),
        x -> Integer.parseInt(x.value(0)));

    assertThat(result).hasValue(5);
  }

  @Test
  void decodeAndMap_shouldThrowForMappingError() {
    var values = List.of("value");
    var encoded = new Cursor(values).encode();
    Function<Cursor, Integer> mapper = x -> {
      throw new RuntimeException("test");
    };

    assertThatCode(() -> Cursor.decoder().decodeAndMap(encoded, values.size(), mapper))
        .isInstanceOf(InvalidCursorException.class)
        .hasRootCauseMessage("test");
  }

  @Test
  void decodeOneAndMap_shouldWork() {
    var encoded = new Cursor("5").encode();

    var result = Cursor.decoder().decodeOneAndMap(
        encoded,
        x -> Integer.parseInt(x));

    assertThat(result).hasValue(5);
  }

  @Test
  void decodeOneToString_shouldWork() {
    var encoded = new Cursor("x").encode();

    var result = Cursor.decoder().decodeOneToString(encoded);
    assertThat(result).hasValue("x");
  }

  @Test
  void decodeTwoAndMap_shouldWork() {
    var encoded = new Cursor("hey", "there").encode();

    var result = Cursor.decoder().decodeTwoAndMap(
        encoded,
        (x, y) -> x + y);

    assertThat(result).hasValue("heythere");
  }

  @Test
  void decodeThreeAndMap_shouldWork() {
    var encoded = new Cursor("hey", "there", "you").encode();

    var result = Cursor.decoder().decodeThreeAndMap(
        encoded,
        (x, y, z) -> x + y + z);

    assertThat(result).hasValue("heythereyou");
  }

  private static class FakeEncryptor {
    public static final String PREFIX = "ENCRYPTED:";

    public static byte[] encrypt(String value) {
      return (PREFIX + value).getBytes(StandardCharsets.UTF_8);
    }

    public static String decrypt(byte[] value) {
      var result = new String(value, StandardCharsets.UTF_8);
      return result.substring(PREFIX.length());
    }
  }
}
