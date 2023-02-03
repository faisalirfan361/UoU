package com.UoU.core.nylas.mapping;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU.core.mapping.WrappedValueMapperImpl;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NylasEventMapperTests {
  private static final NylasEventMapper MAPPER = new NylasEventMapperImpl(
      new NylasParticipantMapperImpl(),
      new WrappedValueMapperImpl());

  @ParameterizedTest
  @ValueSource(strings = {
      "Some Person <some.person@example.com>",
      "  Some Person   <some.person@example.com>  ",
  })
  void toOwnerModelFromNylasString_shouldParseNameAndEmail(String input) {
    val owner = MAPPER.toOwnerModelFromNylasString(input);

    assertThat(owner.name()).isEqualTo("Some Person");
    assertThat(owner.email()).isEqualTo("some.person@example.com");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      // Handle many variations because we've seen weird things from Nylas/providers
      "some.person@example.com",
      "  some.person@example.com  ",
      "<some.person@example.com>",
      " <some.person@example.com>", // this format is common for Google events
      "   <some.person@example.com>   ",
  })
  void toOwnerModelFromNylasString_shouldParseEmailOnly(String input) {
    val owner = MAPPER.toOwnerModelFromNylasString(input);

    assertThat(owner.name()).isNull();
    assertThat(owner.email()).isEqualTo("some.person@example.com");
  }

  @Test
  void toOwnerModelFromNylasString_shouldReturnNullForMissingEmail() {
    val input = "some random string that's not an email";
    val owner = MAPPER.toOwnerModelFromNylasString(input);

    assertThat(owner).isNull();
  }
}
