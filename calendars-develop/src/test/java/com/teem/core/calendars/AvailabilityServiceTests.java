package com.UoU.core.calendars;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;
import static org.mockito.Mockito.mock;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.DataConfig;
import com.UoU.core.TimeSpan;
import com.UoU.core._helpers.ValidatorWrapperFactory;
import com.UoU.core.events.EventsConfig;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ValidationException;
import lombok.val;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTests {
  private static final EventsConfig EVENTS_CONFIG = TestData.eventsConfig();
  private static final AvailabilityService SERVICE = new AvailabilityService(
      mock(AvailabilityRepository.class),
      ValidatorWrapperFactory.createRealInstance(),
      EVENTS_CONFIG);

  /**
   * Ensures validation is triggered by each availability method.
   *
   * <p>More detailed validation tests are elsewhere. The service's responsibility is just to
   * trigger the validation, so it doesn't care about the specific rules.
   */
  @Test
  void allMethods_shouldValidateAndFail() {
    val request = ModelBuilders.availabilityRequest()
        .calendarIds(Stream
            .generate(CalendarId::create)
            .limit(DataConfig.Availability.MAX_CALENDARS + 1)
            .collect(Collectors.toSet()))
        .build();

    assertThatValidationFails(() -> SERVICE.getAvailability(request));
    assertThatValidationFails(() -> SERVICE.getBusyPeriods(request));
    assertThatValidationFails(() -> SERVICE.getDetailedBusyPeriods(request));
  }

  @Test
  void allMethods_shouldCheckActiveEventsPeriod() {
    val request = ModelBuilders.availabilityRequest()
        .orgId(TestData.orgId())
        .calendarIds(Set.of(CalendarId.create()))
        .timeSpan(new TimeSpan(
            EVENTS_CONFIG.activePeriod().current().end().minusSeconds(300),
            EVENTS_CONFIG.activePeriod().current().end().plusSeconds(300)))
        .build();

    assertThatCode(() -> SERVICE.getAvailability(request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("active period");
    assertThatCode(() -> SERVICE.getBusyPeriods(request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("active period");;
    assertThatCode(() -> SERVICE.getDetailedBusyPeriods(request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("active period");;
  }
}
