package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static com.UoU.core.events.ParticipantRequest.builder;
import static com.UoU.core.events.ParticipantStatus.NO;
import static com.UoU.core.events.ParticipantStatus.YES;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.val;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ParticipantRequestTests {

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
    assertThatValidationPasses(buildValid().status(ParticipantStatus.YES).comment("").build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, ParticipantRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(Set.of("email"), builder().build()),
        Arguments.of(Set.of("email"), builder().email("").build()),
        Arguments.of(Set.of("email"), builder().email(" ").status(NO).build()),
        Arguments.of(Set.of("email"), builder().email("invalid").status(NO).build())
    );
  }

  @Test
  void updateFields_shouldBeUnmodifiable() {
    val updateFields = new HashSet<ParticipantRequest.UpdateField>();
    updateFields.add(ParticipantRequest.UpdateField.NAME);

    val request = new ParticipantRequest(null, null, null, null, updateFields);

    AssertionsForClassTypes.assertThatCode(() -> request.updateFields().add(
            ParticipantRequest.UpdateField.COMMENT))
        .isInstanceOf(UnsupportedOperationException.class);
    AssertionsForClassTypes.assertThatCode(() -> request.updateFields().remove(
            ParticipantRequest.UpdateField.NAME))
        .isInstanceOf(UnsupportedOperationException.class);

    AssertionsForInterfaceTypes.assertThat(request.updateFields()).containsExactly(
        ParticipantRequest.UpdateField.NAME);
  }

  @Test
  void hasUpdates_shouldBeTrueWhenUpdateFieldsHasItems() {
    AssertionsForClassTypes.assertThat(builder().name(null).build().hasUpdates()).isTrue();
    AssertionsForClassTypes.assertThat(builder().build().hasUpdates()).isFalse();
  }

  @Test
  void withMatchingUpdateFieldsRemoved_shouldWork() {
    val participant = TestData.participantList(1).get(0);
    val instance = builder()
        .email(participant.email())
        .name(participant.name())
        .comment(TestData.uuidString())
        .build();

    val result = instance.withMatchingUpdateFieldsRemoved(participant);

    // removeMatchingUpdateFields is tested separately, so we only need some basic checks.
    AssertionsForInterfaceTypes.assertThat(result.updateFields())
        .contains(ParticipantRequest.UpdateField.COMMENT)
        .doesNotContain(ParticipantRequest.UpdateField.NAME);
  }

  @Test
  void toBuilder_shouldResultInEqualInstanceWhenNothingChanges() {
    val builder = buildValid().status(NO).comment("test");
    val instance = builder.build();

    AssertionsForClassTypes.assertThat(instance.toBuilder().build())
        .as("toBuilder instance should equal original instance when nothing changed.")
        .isEqualTo(instance);

    AssertionsForClassTypes.assertThat(instance.toBuilder().name(null).build())
        .as("toBuilder instance should NOT equal original instance when something changed.")
        .isNotEqualTo(instance);
  }

  private ParticipantRequest.Builder buildValid() {
    return builder().name(TestData.uuidString()).email(TestData.email());
  }

  static class BuilderTests {

    @ParameterizedTest
    @EnumSource(ParticipantRequest.UpdateField.class)
    void build_shouldAddSetFieldToUpdateFields(ParticipantRequest.UpdateField field) {
      val builder = builder();

      AssertionsForInterfaceTypes.assertThat(builder.build().updateFields())
          .as("Builder should have no updateFields to start.")
          .isEmpty();

      // Apply setter for single field.
      switch (field) {
        case NAME -> builder.name(null);
        case STATUS -> builder.status(null);
        case COMMENT -> builder.comment(null);
        default -> throw new IllegalArgumentException("Unhandled enum value");
      }

      AssertionsForInterfaceTypes.assertThat(builder.build().updateFields())
          .as("Single set field should be in updateFields.")
          .containsExactly(field);
    }

    @ParameterizedTest
    @EnumSource(ParticipantRequest.UpdateField.class)
    void removeMatchingUpdateFieldsRemoved_shouldKeepChangedField(
        ParticipantRequest.UpdateField field) {

      val participant = ModelBuilders.participant()
          .name("test")
          .email(TestData.email())
          .status(NO)
          .comment("test")
          .build();
      Supplier<ParticipantRequest.Builder> matchingBuilder = () -> builder()
          .email(participant.email())
          .name(participant.name())
          .status(participant.status())
          .comment(participant.comment());

      val builder1 = matchingBuilder.get();
      builder1.removeMatchingUpdateFields(participant);
      AssertionsForInterfaceTypes.assertThat(builder1.build().updateFields())
          .as("Update fields should be empty before changing the builder value")
          .isEmpty();

      // Create another builder with single changed field:
      val builder2 = matchingBuilder.get();
      switch (field) {
        case NAME -> builder2.name(TestData.uuidString());
        case STATUS -> builder2.status(YES);
        case COMMENT -> builder2.comment(TestData.uuidString());
        default -> throw new IllegalArgumentException("Unhandled enum value");
      }

      builder2.removeMatchingUpdateFields(participant);
      AssertionsForInterfaceTypes.assertThat(builder2.build().updateFields())
          .as(field + " should be the only update field after changing the builder value")
          .containsExactly(field);
    }
  }
}
