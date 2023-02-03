package com.UoU._integration.redis;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.SaveRequest;
import com.UoU.core.diagnostics.Status;
import com.UoU.core.diagnostics.events.RunEvent;
import com.UoU.core.exceptions.NotFoundException;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;

public class RedisDiagnosticRepositoryTests extends BaseAppIntegrationTest {

  @Test
  void getOrSaveCurrentRun_shouldOnlyCreateOnce() {
    val calendarId = CalendarId.create();

    val results = Stream
        .generate(() -> redisHelper.getDiagnosticRepo().getOrSaveCurrentRun(calendarId))
        .limit(3)
        .toList();

    assertThat(results.size()).isEqualTo(3);
    assertThat(results.get(0).isNew()).isTrue();
    assertThat(results.get(1).isNew()).isFalse();
    assertThat(results.get(2).isNew()).isFalse();
  }

  @Test
  void getOrSaveCurrentRun_shouldSetPendingStatus() {
    val calendarId = CalendarId.create();

    val runId = redisHelper.getDiagnosticRepo().getOrSaveCurrentRun(calendarId).runId();
    val status = redisHelper.getDiagnosticRepo().getResults(runId).status();

    assertThat(status).isEqualTo(Status.PENDING);
  }

  @Test
  void getStatus_shouldWork() {
    val request = SaveRequest.builder()
        .runId(createRunId())
        .status(Status.PROCESSING)
        .build();
    redisHelper.getDiagnosticRepo().save(request);

    val status = redisHelper.getDiagnosticRepo().getStatus(request.runId());

    assertThat(status).isEqualTo(request.status());
  }

  @Test
  void getStatus_shouldThrowNotFound() {
    val runId = createRunId();

    assertThatCode(() -> redisHelper.getDiagnosticRepo().getStatus(runId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Run");
  }

  @Test
  void getResults_shouldWork() {
    val request = SaveRequest.builder()
        .runId(createRunId())
        .status(Status.PROCESSING)
        .newEvent(new RunEvent.RunStarted())
        .build();
    redisHelper.getDiagnosticRepo().save(request);

    val results = redisHelper.getDiagnosticRepo().getResults(request.runId());

    assertThat(results.status()).isEqualTo(Status.PROCESSING);
    assertThat(results.startedAt()).isNull();
    assertThat(results.finishedAt()).isNull();
    assertThat(results.duration()).isEmpty();
    assertThat(results.expiresAt()).isAfter(Instant.now());
    assertThat(results.events()).hasSize(1);
    assertThat(results.events().get(0)).isInstanceOf(RunEvent.RunStarted.class);
  }

  @Test
  void getResults_shouldThrowNotFound() {
    val runId = createRunId();

    assertThatCode(() -> redisHelper.getDiagnosticRepo().getResults(runId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Run");
  }

  @Test
  void save_shouldSaveOnlyNonNullProperties() {
    val runId = createRunId();
    val firstRequest = SaveRequest.builder()
        .runId(runId)
        .status(Status.FAILED)
        .build();
    val secondRequest = SaveRequest.builder()
        .runId(runId)
        .startedAt(Instant.now())
        .build();

    redisHelper.getDiagnosticRepo().save(firstRequest);
    redisHelper.getDiagnosticRepo().save(secondRequest);

    val results = redisHelper.getDiagnosticRepo().getResults(runId);
    assertThat(results.status()).isEqualTo(firstRequest.status());
    assertThat(results.startedAt()).isEqualTo(secondRequest.startedAt());
  }

  private static RunId createRunId() {
    return new RunId(CalendarId.create(), UUID.randomUUID());
  }
}
