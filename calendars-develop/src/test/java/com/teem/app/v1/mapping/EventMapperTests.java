package com.UoU.app.v1.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import com.UoU._helpers.TestData;
import com.UoU.app.security.Principal;
import com.UoU.app.v1.dtos.EventCreateRequestDto;
import com.UoU.app.v1.dtos.EventUpdateRequestDto;
import com.UoU.app.v1.dtos.WhenDto;
import com.UoU.core.events.EventId;
import com.UoU.core.events.When;
import com.UoU.core.mapping.WrappedValueMapperImpl;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Tests the MapStruct-generated EventMapperImpl for some tricky mappings.
 */
public class EventMapperTests {
  private static final EventMapper MAPPER = new EventMapperImpl(new WrappedValueMapperImpl());

  @Test
  void shouldMapToEventCreateRequestModel() {
    var id = EventId.create();
    var when = new WhenDto.TimeSpan(TestData.instant(), TestData.instant());
    var dto = new EventCreateRequestDto(
        TestData.uuidString(), "title", "desc", "location", when, null, false, List.of(), null,
        null);

    var principal = new Principal(new Jwt(
        "x",
        Instant.now(),
        Instant.now().plus(1, ChronoUnit.HOURS),
        Map.of("test", "test"),
        Map.of("org_id", "test", "subject", TestData.email())));
    var model = MAPPER.toRequestModel(dto, id, principal);

    assertThat(model.id().value()).isEqualTo(id.value());
    assertThat(model.when()).isNotNull();
  }

  @Test
  void shouldMapToEventUpdateRequestModel() {
    var id = EventId.create();
    var when = new WhenDto.DateSpan(TestData.localDate(), TestData.localDate());
    var dto = new EventUpdateRequestDto(
        "title", "desc", "location", when, null, false, List.of(), null);

    var model = MAPPER.toRequestModel(dto, id, TestData.orgId());

    assertThat(model.id().value()).isEqualTo(id.value());
    assertThat(model.when()).isNotNull();
  }

  @Test
  void shouldMapToEventDto() {
    var model = TestData.event();
    var dto = MAPPER.toEventDto(model);
    assertThat(dto.id()).isEqualTo(model.id().value());
  }

  @Test
  void shouldMapToWhenDtoAndBackToModel() {
    var model = TestData.whenTimeSpan();

    var dto = (WhenDto.TimeSpan) MAPPER.toWhenDto(model);
    var mappedModel = (When.TimeSpan) MAPPER.toWhenModel(dto);

    assertThat(dto.startTime()).isEqualTo(model.startTime());
    assertThat(dto.endTime()).isEqualTo(model.endTime());
    assertThat(mappedModel).isEqualTo(model);
  }
}
