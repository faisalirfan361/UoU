package com.UoU.infra.db.mapping;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.infra.db._helpers.Mappers;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class JooqEventMapperTests {
  private static final JooqEventMapper MAPPER = Mappers.EVENT_MAPPER;

  private static final Supplier<ZoneId> ZONE_SUPPLIER = () -> ZoneId.of("America/Chicago");

  @Test
  void toRecord_fromEventCreateRequest_shouldSetCreatedAt() {
    var request = ModelBuilders.eventCreateRequestWithTestData().build();
    var record = MAPPER.toRecord(request, ZONE_SUPPLIER);

    assertThat(record.getCreatedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
    assertThat(record.getUpdatedAt()).isNull();
  }

  @Test
  void toRecord_fromEventCreatRequest_shouldSetTimeFields() {
    var timeSpan = TestData.whenTimeSpan();
    var request = ModelBuilders.eventUpdateRequestWithTestData()
        .when(timeSpan)
        .build();
    var record = MAPPER.toRecord(request, ZONE_SUPPLIER);

    assertThat(record.getIsAllDay()).isFalse();
    assertThat(record.getAllDayStartAt()).isNull();
    assertThat(record.getAllDayEndAt()).isNull();
    assertThat(record.getStartAt().toInstant()).isEqualTo(timeSpan.startTime());
    assertThat(record.getEndAt().toInstant()).isEqualTo(timeSpan.endTime());
  }

  @Test
  void toRecord_fromEventUpdateRequest_shouldSetUpdatedAt() {
    var request = ModelBuilders.eventUpdateRequestWithTestData().build();
    var record = MAPPER.toRecord(request, ZONE_SUPPLIER);

    assertThat(record.getCreatedAt()).isNull();
    assertThat(record.getUpdatedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
  }

  @Test
  void toRecord_fromEventUpdateRequest_shouldSetTimeFields() {
    var dateSpan = TestData.whenDateSpan();
    var request = ModelBuilders.eventUpdateRequestWithTestData()
        .when(dateSpan)
        .build();
    var record = MAPPER.toRecord(request, ZONE_SUPPLIER);
    var utcTimeSpan = request.when().toUtcTimeSpan(ZONE_SUPPLIER);

    assertThat(record.getIsAllDay()).isTrue();
    assertThat(record.getAllDayStartAt()).isEqualTo(dateSpan.startDate());
    assertThat(record.getAllDayEndAt()).isEqualTo(dateSpan.endDate());
    assertThat(record.getStartAt().toInstant()).isEqualTo(utcTimeSpan.start());
    assertThat(record.getEndAt().toInstant()).isEqualTo(utcTimeSpan.end());
  }
}
