package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EventRequestTests {

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
    assertThatValidationPasses(buildValid().dataSource(null).build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, EventRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("id", "orgId"),
            ModelBuilders.eventRequest().build()), // everything is null
        Arguments.of(
            Set.of("orgId.value"),
            buildValid().orgId(new OrgId(" ")).build()),
        Arguments.of(
            Set.of("dataSource.value"),
            buildValid().dataSource(new DataSource(" ")).build())
    );
  }

  private static ModelBuilders.EventRequestBuilder buildValid() {
    return ModelBuilders.eventRequest()
        .eventId(EventId.create())
        .orgId(TestData.orgId())
        .dataSource(DataSource.PROVIDER);
  }
}
