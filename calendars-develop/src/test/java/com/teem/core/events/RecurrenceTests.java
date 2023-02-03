package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.UoU._helpers.TestData;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class RecurrenceTests {

  @Test
  void withMaster_IsMaster_withInstance_isInstance_shouldBeMutuallyExclusive() {
    var master = TestData.recurrenceMaster();
    var instance = TestData.recurrenceInstance();

    assertThat(master.withMaster()).isPresent();
    assertThat(master.withInstance()).isEmpty();
    assertThat(master.isMaster()).isTrue();

    assertThat(instance.withInstance()).isPresent();
    assertThat(instance.withMaster()).isEmpty();
    assertThat(instance.isInstance()).isTrue();
  }

  @Test
  void isInstanceThat_shouldCheckCondition() {
    var instance = TestData.recurrenceInstance();

    assertThat(instance.isInstanceThat(x -> true)).isTrue();
    assertThat(instance.isInstanceThat(x -> false)).isFalse();
  }

  @Test
  void instance_isInSeries_shouldCheckMasterId() {
    var masterId = EventId.create();
    var instance = Recurrence.instance(masterId, false);

    assertThat(instance.getInstance().isInSeries(masterId)).isTrue();
    assertThat(instance.getInstance().isInSeries(EventId.create())).isFalse();
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldPass(Recurrence recurrence) {
    assertThatValidationPasses(recurrence);
  }

  private static Stream<Recurrence> validation_shouldPass() { // test data
    return Stream.of(
        Recurrence.none(),
        TestData.recurrenceInstance(),
        TestData.recurrenceMaster(),
        masterWithRrule(
            "RRULE:FREQ=DAILY;UNTIL=20220511T140000",
            "EXDATE:20220513T140000Z,20220512T140000"));
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, Recurrence recurrence) {
    assertThatValidationFails(invalidProps, recurrence);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    val validRrule = TestData.recurrenceMaster().getMaster().rrule();
    val general = Stream.of(
        Arguments.of(
            Set.of("master.rrule", "master.timezone"),
            Recurrence.master(new Recurrence.Master(null, null))),
        Arguments.of(
            Set.of("master.rrule", "master.timezone"),
            Recurrence.master(List.of("", "     "), "")),
        Arguments.of(
            Set.of("master.timezone"),
            Recurrence.master(validRrule, "america/invalid")));

    // The rrule list must contain one valid RRULE and optionally one valid EXDATE expression.
    val rrule = Stream.of(
        masterWithRrule(),
        masterWithRrule("RRULE:invalid"),
        masterWithRrule("RRULE:FREQ=DAILY;UNTIL=20220511T140000", "invalid"),
        masterWithRrule("RRULE:FREQ=DAILY;UNTIL=20220511T140000", "invalid"),
        masterWithRrule("RRULE:FREQ=DAILY;UNTIL=20220511T140000", "EXDATE:20220513T140000Z", "x"),
        masterWithRrule("RRULE:FREQ=DAILY;UNTIL=20220511T140000", "EXDATE:20220513 140000Z"),
        masterWithRrule("RRULE:FREQ=DAILY;UNTIL=20220511T140000", "RRULE:FREQ=MONTHLY")
    ).map(r -> Arguments.of(Set.of("master.rrule"), r));

    return Stream.concat(general, rrule);
  }

  @Test
  void validation_shouldFailOnTrailingSemicolon() {
    assertThatValidationFails(Set.of("master.rrule"), masterWithRrule("RRULE:FREQ=DAILY;"))
        .extracting(x -> x.getConstraintViolations())
        .matches(x -> x.size() == 1)
        .extracting(x -> x.stream().findFirst().orElseThrow().getMessage())
        .matches(x -> x.contains("semicolon"), "is error about trailing semicolon");
  }

  @Test
  void validation_shouldFailOnRruleThatViolatesSpecAndFailsLibRecurParsing() {
    // We don't need to validate against spec because that's lib-recur's job. We just want to ensure
    // lib-recur parsing exceptions result in a custom message, so we just need one invalid rule.
    val rrule = "RRULE:FREQ=DAILY;COUNT=2;UNTIL=20220511"; // cannot have both COUNT and UNTIL
    assertThatValidationFails(Set.of("master.rrule"), masterWithRrule(rrule))
        .extracting(x -> x.getConstraintViolations())
        .matches(x -> x.size() == 1)
        .extracting(x -> x.stream().findFirst().orElseThrow().getMessage())
        .matches(x -> x.contains("UNTIL and COUNT"), "is error about UNTIL and COUNT in same rule");
  }

  @ParameterizedTest
  @ValueSource(strings = {"UNTIL=20220511", "UNTIL=20220511T140000"})
  void validation_shouldFailOnInvalidUntilForNonAllDayEvent(String until) {
    assertThatValidationFails(
        Set.of("master.rrule"),
        masterWithRruleAndValidationContext(false, "RRULE:FREQ=DAILY;" + until))
        .extracting(x -> x.getConstraintViolations())
        .matches(x -> x.size() == 1)
        .extracting(x -> x.stream().findFirst().orElseThrow().getMessage())
        .matches(
            x -> x.contains("UNTIL must be a UTC datetime"),
            "is error about UNTIL not being UTC datetime");
  }

  @ParameterizedTest
  @ValueSource(strings = {"UNTIL=20220511T140000", "UNTIL=20220511T140000Z"})
  void validation_shouldFailOnInvalidUntilForAllDayEvent(String until) {
    assertThatValidationFails(
        Set.of("master.rrule"),
        masterWithRruleAndValidationContext(true, "RRULE:FREQ=DAILY;" + until))
        .extracting(x -> x.getConstraintViolations())
        .matches(x -> x.size() == 1)
        .extracting(x -> x.stream().findFirst().orElseThrow().getMessage())
        .matches(
            x -> x.contains("UNTIL must be a date without time"),
            "is error about UNTIL not being a date without time");
  }

  private static Recurrence masterWithRrule(String... rrule) {
    return Recurrence.master(List.of(rrule), "America/Denver");
  }

  private static Recurrence masterWithRruleAndValidationContext(boolean isAllDay, String... rrule) {
    return Recurrence.master(
        List.of(rrule),
        "America/Chicago",
        new Recurrence.Master.ValidationContext(isAllDay));
  }
}
