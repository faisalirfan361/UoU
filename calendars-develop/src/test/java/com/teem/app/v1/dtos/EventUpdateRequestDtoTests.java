package com.UoU.app.v1.dtos;

import static com.UoU.app.v1.dtos.ParticipantDto.Status.NO_REPLY;
import static com.UoU.app.v1.dtos.ParticipantDto.Status.YES;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
import java.util.Locale;
import lombok.val;
import org.junit.jupiter.api.Test;

class EventUpdateRequestDtoTests {
  @Test
  void shouldReadAndWriteJson() {
    val inputJson = "{"
        + "\"title\":\"title\","
        + "\"description\":\"description\","
        + "\"location\":\"location\","
        + "\"when\":{"
        + "\"startTime\":\"2022-01-01T13:00:00Z\","
        + "\"endTime\":\"2022-01-01T14:00:00Z\","
        + "\"type\":\"timespan\"},"
        + "\"recurrence\":null,"
        + "\"isBusy\":true,"
        + "\"participants\":["
        + "{\"name\":\"Participant 1\","
        + "\"email\":\"p1@test.com\","
        + "\"status\":\"yes\","
        + "\"comment\":\"comment\"},"
        + "{\"name\":\"Participant 2\","
        + "\"email\":\"p2@test.com\","
        + "\"status\":\"noreply\","
        + "\"comment\":\"comment\"}],"
        + "\"dataSource\":null}";

    JsonTestHelper.shouldReadAndWriteJson(inputJson, EventUpdateRequestDto.class, (json, dto) -> {
      assertThat(dto.title()).isEqualTo("title");
      assertThat(dto.description()).isEqualTo("description");
      assertThat(dto.location()).isEqualTo("location");

      val when = (WhenDto.TimeSpan) dto.when();
      assertThat(when.startTime()).isEqualTo(Instant.parse("2022-01-01T13:00:00Z"));
      assertThat(when.endTime()).isEqualTo(Instant.parse("2022-01-01T14:00:00Z"));
      assertThat(when.type().name().toLowerCase(Locale.ROOT)).isEqualTo("timespan");

      val participants = dto.participants();
      assertThat(participants.size()).isEqualTo(2);
      assertThat(participants.get(0).name()).isEqualTo("Participant 1");
      assertThat(participants.get(0).email()).isEqualTo("p1@test.com");
      assertThat(participants.get(0).status()).isEqualTo(YES);
      assertThat(participants.get(0).comment()).isEqualTo("comment");
      assertThat(participants.get(1).name()).isEqualTo("Participant 2");
      assertThat(participants.get(1).email()).isEqualTo("p2@test.com");
      assertThat(participants.get(1).status()).isEqualTo(NO_REPLY);
      assertThat(participants.get(1).comment()).isEqualTo("comment");
    });
  }
}

