package com.UoU.core.diagnostics;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core._helpers.ValidatorWrapperFactory;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.diagnostics.tasks.TaskScheduler;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.exceptions.ReadOnlyException;
import com.UoU.core.validation.ValidatorWrapper;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class DiagnosticServiceTests {

  @Test
  void run_shouldValidateRequestObject() {
    val scenario = new Scenario()
        .withAccount()
        .withCalendar();
    val request = scenario.buildRequest()
        .calendarId(null) // invalid
        .build();

    assertThatValidationFails(() -> scenario.service.run(request));
  }

  @Test
  void run_shouldValidateOrg() {
    val scenario = new Scenario()
        .withAccount()
        .withCalendar();
    val request = scenario.buildRequest()
        .orgId(new OrgId("different"))
        .build();

    assertThatCode(() -> scenario.service.run(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Calendar");
  }

  @Test
  void run_shouldValidateWritableCalendar() {
    val scenario = new Scenario()
        .withAccount()
        .withReadOnlyCalendar();
    val request = scenario.buildRequest().build();

    assertThatCode(() -> scenario.service.run(request))
        .isInstanceOf(ReadOnlyException.class)
        .hasMessageContaining("calendar")
        .hasMessageContaining("read-only");
  }

  @Test
  void run_shouldValidateCalendarIsEligibleToSync() {
    val scenario = new Scenario().withCalendar(); // no account
    val request = scenario.buildRequest().build();

    assertThatCode(() -> scenario.service.run(request))
        .isInstanceOf(IllegalOperationException.class)
        .hasMessageContaining("calendar")
        .hasMessageContaining("account");
  }

  @Test
  void run_shouldWork() {
    val scenario = new Scenario().withAccount().withCalendar();
    val request = scenario.buildRequest().build();

    val id = scenario.service.run(request);
    assertThat(id).isNotNull();
  }

  private static class Scenario {
    private OrgId orgId = TestData.orgId();
    private Optional<AccountId> accountId = Optional.empty();
    private Optional<CalendarId> calendarId = Optional.empty();
    private Optional<CalendarExternalId> calendarExternalId = Optional.empty();
    private boolean isCalendarReadOnly = false;
    private Scenario.Dependencies deps = new Scenario.Dependencies(
        mock(DiagnosticRepository.class),
        mock(CalendarRepository.class),
        ValidatorWrapperFactory.createRealInstance(),
        mock(TaskScheduler.class));
    private DiagnosticService service = new DiagnosticService(
        deps.diagnosticRepoMock,
        deps.calendarRepoMock,
        deps.validator,
        deps.taskSchedulerMock);

    public Scenario() {
      when(deps.calendarRepoMock.get(any(CalendarId.class)))
          .then(inv -> calendarId
              .filter(id -> id.equals(inv.getArgument(0)))
              .map(x -> ModelBuilders.calendar()
                  .id(x)
                  .externalId(calendarExternalId.orElse(null))
                  .orgId(orgId)
                  .accountId(accountId.orElse(null))
                  .isReadOnly(isCalendarReadOnly)
                  .name("test")
                  .build())
              .orElseThrow(() -> NotFoundException.ofClass(Calendar.class)));

      when(deps.calendarRepoMock.getAccountId(any(CalendarId.class)))
          .then(inv -> accountId
              .filter(x -> calendarId
                  .filter(id -> id.equals(inv.getArgument(0)))
                  .isPresent()));

      when(deps.diagnosticRepoMock.getOrSaveCurrentRun(any(CalendarId.class)))
          .then(inv -> calendarId
              .filter(id -> id.equals(inv.getArgument(0)))
              .map(x -> new RunIdInfo(new RunId(x, UUID.randomUUID()), true))
              .orElseThrow());
    }

    public Scenario withAccount() {
      accountId = Optional.of(TestData.accountId());
      return this;
    }

    public Scenario withCalendar() {
      calendarId = Optional.of(CalendarId.create());
      calendarExternalId = Optional.of(TestData.calendarExternalId());
      isCalendarReadOnly = false;
      return this;
    }

    public Scenario withReadOnlyCalendar() {
      calendarId = Optional.of(CalendarId.create());
      calendarExternalId = Optional.of(TestData.calendarExternalId());
      isCalendarReadOnly = true;
      return this;
    }

    public ModelBuilders.DiagnosticRequestBuilder buildRequest() {
      return ModelBuilders.diagnosticRequest()
          .orgId(orgId)
          .calendarId(calendarId.orElse(null));
    }

    private record Dependencies(
        DiagnosticRepository diagnosticRepoMock,
        CalendarRepository calendarRepoMock,
        ValidatorWrapper validator,
        TaskScheduler taskSchedulerMock) {
    }
  }
}
