package com.UoU.core.calendars;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.val;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class CalendarUpdateRequestTests {

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, CalendarUpdateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("id", "orgId"),
            new CalendarUpdateRequest(null, null, null, null, null, null)), // everything is null
        Arguments.of(
            Set.of("name"),
            buildValid().name("x".repeat(CalendarConstraints.NAME_MAX + 1)).build()),
        Arguments.of(
            Set.of("timezone"),
            buildValid().timezone("invalid").build())
    );
  }

  @Test
  void updateFields_shouldBeUnmodifiable() {
    val updateFields = new HashSet<CalendarUpdateRequest.UpdateField>();
    updateFields.add(CalendarUpdateRequest.UpdateField.TIMEZONE);

    val request = new CalendarUpdateRequest(null, null, null, null, null, updateFields);

    assertThatCode(() -> request.updateFields().add(CalendarUpdateRequest.UpdateField.NAME))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(request.updateFields()).containsExactly(CalendarUpdateRequest.UpdateField.TIMEZONE);
  }

  @Test
  void hasUpdates_shouldBeTrueWhenUpdateFieldsHasItems() {
    AssertionsForClassTypes.assertThat(builder().name(null).build().hasUpdates()).isTrue();
    AssertionsForClassTypes.assertThat(builder().build().hasUpdates()).isFalse();
  }

  @Test
  void hasUpdates_shouldBeFalseWhenUpdateFieldsIsEmpty() {
    AssertionsForClassTypes.assertThat(builder().build().hasUpdates()).isFalse();
  }

  @Test
  void hasUpdate_shouldBeTrueWhenUpdateFieldsHasSpecifiedField() {
    val field = CalendarUpdateRequest.UpdateField.NAME;
    AssertionsForClassTypes.assertThat(builder().name(null).build().hasUpdate(field)).isTrue();
    AssertionsForClassTypes.assertThat(builder().timezone(null).build().hasUpdate(field)).isFalse();
  }

  @Test
  void withMatchingUpdateFieldsRemoved_shouldWork() {
    val calendar = ModelBuilders.calendarWithTestData().build();
    val instance = builder()
        .id(calendar.id())
        .name(TestData.uuidString())
        .timezone(calendar.timezone())
        .build();

    val result = instance.withMatchingUpdateFieldsRemoved(calendar);

    // removeMatchingUpdateFields is tested separately, so we only need some basic checks.
    assertThat(result.updateFields())
        .contains(CalendarUpdateRequest.UpdateField.NAME)
        .doesNotContain(CalendarUpdateRequest.UpdateField.TIMEZONE);
  }

  @Test
  void toBuilder_shouldResultInEqualInstanceWhenNothingChanges() {
    val builder = buildValid();
    val instance = builder.build();

    assertThat(instance.toBuilder().build())
        .as("toBuilder instance should equal original instance when nothing changed.")
        .isEqualTo(instance);

    assertThat(instance.toBuilder().name(null).build())
        .as("toBuilder instance should NOT equal original instance when something changed.")
        .isNotEqualTo(instance);
  }

  private static CalendarUpdateRequest.Builder builder() {
    return CalendarUpdateRequest.builder().id(CalendarId.create());
  }

  private static CalendarUpdateRequest.Builder buildValid() {
    return builder()
        .id(CalendarId.create())
        .orgId(TestData.orgId())
        .name("Test")
        .timezone("UTC")
        .isReadOnly(true);
  }

  static class BuilderTests {

    @Test
    void build_shouldRequireId() {
      val builder = CalendarUpdateRequest.builder();

      assertThatCode(builder::build)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("id");
    }

    @ParameterizedTest
    @EnumSource(CalendarUpdateRequest.UpdateField.class)
    void build_shouldAddSetFieldToUpdateFields(CalendarUpdateRequest.UpdateField field) {
      val builder = builder();

      assertThat(builder.build().updateFields())
          .as("Builder should have no updateFields to start.")
          .isEmpty();

      // Apply setter for single field.
      switch (field) {
        case NAME -> builder.name(null);
        case IS_READ_ONLY -> builder.isReadOnly(false);
        case TIMEZONE -> builder.timezone(null);
        default -> throw new IllegalArgumentException("Unhandled enum value");
      }

      assertThat(builder.build().updateFields())
          .as("Single set field should be in updateFields.")
          .containsExactly(field);
    }

    @ParameterizedTest
    @EnumSource(CalendarUpdateRequest.UpdateField.class)
    void removeMatchingUpdateFields_shouldKeepChangedField(
        CalendarUpdateRequest.UpdateField field) {

      val calendar = ModelBuilders.calendarWithTestData().build();

      // Create builder that uses values matching the calendar but in copied objects so we can test
      // that equality is based on values and not object references.
      Supplier<CalendarUpdateRequest.Builder> builder = () -> CalendarUpdateRequest.builder()
          .id(new CalendarId(calendar.id().value()))
          .orgId(new OrgId(calendar.orgId().value()))
          .name(calendar.name())
          .isReadOnly(calendar.isReadOnly())
          .timezone(calendar.timezone());

      val builder1 = builder.get();
      builder1.removeMatchingUpdateFields(calendar);
      assertThat(builder1.build().updateFields())
          .as("Update fields should be empty before changing the builder value")
          .isEmpty();

      // Create another builder with single changed field:
      val builder2 = builder.get();
      switch (field) {
        case NAME -> builder2.name(TestData.uuidString());
        case IS_READ_ONLY -> builder2.isReadOnly(!calendar.isReadOnly());
        case TIMEZONE -> builder2.timezone(TestData.timezoneOtherThan(calendar.timezone()));
        default -> throw new IllegalArgumentException("Unhandled enum value");
      }

      builder2.removeMatchingUpdateFields(calendar);
      assertThat(builder2.build().updateFields())
          .as(field + " should be the only update field after changing the builder value")
          .containsExactly(field);
    }
  }
}
