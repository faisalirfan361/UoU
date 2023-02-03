package com.UoU.core.calendars;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.UoU.core.DataConfig;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import lombok.val;
import org.junit.jupiter.api.Test;

class ZoneParserSupplierTests {

  @Test
  void shouldParseValidZoneId() {
    val supplier = new ZoneParserSupplier(() -> "America/Denver", true);
    val result = supplier.get();

    assertThat(result).isEqualTo(ZoneId.of("America/Denver"));
  }

  @Test
  void shouldUseDefaultOnFailure() {
    val supplier = new ZoneParserSupplier(() -> "not-a-timezone", true);
    val result = supplier.get();

    assertThat(result).isEqualTo(DataConfig.Calendars.DEFAULT_TIMEZONE);
  }

  @Test
  void shouldThrowOnFailure() {
    val supplier = new ZoneParserSupplier(() -> "not-a-timezone", false);

    assertThatCode(() -> supplier.get())
        .isInstanceOf(ZoneRulesException.class)
        .hasMessageContaining("not-a-timezone");
  }
}
