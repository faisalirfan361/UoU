package com.UoU.core.diagnostics.tasks;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU.core.Noop;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.diagnostics.Config;
import com.UoU.core.diagnostics.DiagnosticRepository;
import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.Status;
import com.UoU.core.events.EventRepository;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.tasks.DeleteEventFromNylasTask;
import com.UoU.core.nylas.tasks.ExportEventToNylasTask;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Tests some basic stuff for the task.
 *
 * <p>There's too many external dependencies to make mocking and testing the whole task worth it.
 * We just want to test the initial preconditions and make sure the workflow starts correctly.
 */
class CalendarSyncDiagnosticTaskTests {

  private RunId runId;
  private CalendarRepository calendarRepoMock;
  private DiagnosticRepository diagnosticRepoMock;
  private CalendarSyncDiagnosticTask task;
  private CalendarSyncDiagnosticTask.Params params;

  @BeforeEach
  void setUp() {
    runId = new RunId(CalendarId.create(), UUID.randomUUID());

    diagnosticRepoMock = mock(DiagnosticRepository.class);

    calendarRepoMock = mock(CalendarRepository.class);
    when(calendarRepoMock.get(runId.calendarId()))
        .thenReturn(ModelBuilders.calendarWithTestData().id(runId.calendarId()).build());

    task = new CalendarSyncDiagnosticTask(
        new Config(
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            new Config.ProviderSyncWait(1, Duration.ofSeconds(1))),
        diagnosticRepoMock,
        mock(AccountRepository.class),
        calendarRepoMock,
        mock(EventRepository.class),
        mock(ExportEventToNylasTask.class),
        mock(DeleteEventFromNylasTask.class),
        mock(NylasClientFactory.class),
        mock(RunnerFactory.class),
        mock(RestTemplate.class));
    params = new CalendarSyncDiagnosticTask.Params(runId, "https://callback.example.com");
  }

  @Test
  void shouldStartBySavingStatusAndStartedAt() {
    when(diagnosticRepoMock.getStatus(runId))
        .thenReturn(Status.PENDING);

    try {
      task.run(params);
    } catch (Exception ex) {
      Noop.because("failure is expected because we haven't setup/mocked the entire workflow");
    }

    verify(diagnosticRepoMock).save(argThat(x ->
        x.runId() == runId
            && x.status() == Status.PROCESSING
            && x.startedAt() != null));
  }

  @Test
  void shouldThrowIfNotPending() {
    when(diagnosticRepoMock.getStatus(runId))
        .thenReturn(Status.PROCESSING);

    assertThatCode(() -> task.run(params))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("pending");
  }
}
