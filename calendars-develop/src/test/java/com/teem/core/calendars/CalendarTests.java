package com.UoU.core.calendars;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.ReadOnlyException;
import lombok.val;
import org.junit.jupiter.api.Test;

class CalendarTests {

  @Test
  void isEligibleToSync_shouldBeTrueWhenEligible() {
    val calendar = buildEligible().build();
    val result = calendar.isEligibleToSync();
    assertThat(result).isTrue();
  }

  @Test
  void isEligibleToSync_shouldBeFalseWhenEligible() {
    val calendar = buildEligible().accountId(null).build();
    val result = calendar.isEligibleToSync();
    assertThat(result).isFalse();
  }

  @Test
  void requireIsEligibleToSync_shouldNotThrowWhenEligible() {
    val calendar = buildEligible().build();
    assertThatCode(calendar::requireIsEligibleToSync).doesNotThrowAnyException();
  }

  @Test
  void requireIsEligibleToSync_shouldRequireWritable() {
    val calendar = buildEligible().isReadOnly(true).build();
    assertThatCode(calendar::requireIsEligibleToSync)
        .isInstanceOf(ReadOnlyException.class)
        .hasMessageContaining("read-only");
  }

  @Test
  void requireIsEligibleToSync_shouldRequireExternalId() {
    val calendar = buildEligible().externalId(null).build();
    assertThatCode(calendar::requireIsEligibleToSync)
        .isInstanceOf(IllegalOperationException.class)
        .hasMessageContaining("external");
  }

  @Test
  void requireIsEligibleToSync_shouldRequireAccountId() {
    val calendar = buildEligible().accountId(null).build();
    assertThatCode(calendar::requireIsEligibleToSync)
        .isInstanceOf(IllegalOperationException.class)
        .hasMessageContaining("account");
  }

  private static ModelBuilders.CalendarBuilder buildEligible() {
    return ModelBuilders.calendarWithTestData()
        .externalId(TestData.calendarExternalId())
        .accountId(TestData.accountId())
        .isReadOnly(false);
  }
}
