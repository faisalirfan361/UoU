package com.UoU.core.calendars;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertViolationExceptionForField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core._helpers.ValidatorWrapperFactory;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.Provider;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.exceptions.ReadOnlyException;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import lombok.val;
import org.junit.jupiter.api.Test;

class CalendarServiceTests {

  @Test
  void get_shouldWork() {
    val scenario = new Scenario();
    val calendar = scenario.service.get(scenario.orgId, scenario.calendarId);
    assertThat(calendar.id()).isEqualTo(scenario.calendarId);
  }

  @Test
  void get_shouldThrowForCalendarInDifferentOrg() {
    val scenario = new Scenario();

    assertThatCode(() -> scenario.service.get(TestData.orgId(), scenario.calendarId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Calendar");
  }

  @Test
  void createInternal_shouldCreateCalendarAndScheduleExport() {
    val scenario = new Scenario();
    val name = TestData.uuidString();
    val request = new InternalCalendarCreateRequest(TestData.orgId(), name, "UTC");

    val result = scenario.service.createInternal(request);

    assertThat(result.id()).isNotNull();
    assertThat(result.name()).isEqualTo(name);
    assertThat(result.email()).contains("@");

    verify(scenario.deps.calendarRepoMock).create(argThat(x -> x.name().equals(name)));
    verify(scenario.deps.nylasTaskSchedulerMock).exportCalendarsToNylas(
        argThat(x -> x.size() == 1 && x.contains(result.id())),
        eq(false));
  }

  @Test
  void createInternal_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = new InternalCalendarCreateRequest(TestData.orgId(), "name", "invalid");

    assertThatValidationFails(() -> scenario.service.createInternal(request))
        .hasMessageContaining(InternalCalendarCreateRequest.class.getSimpleName());
  }

  @Test
  void batchCreateInternal_shouldCreateCalendarsAndScheduleExport() {
    val scenario = new Scenario();
    val request = ModelBuilders.internalCalendarBatchCreateRequestWithTestData()
        .namePattern("{n}")
        .start(1)
        .end(3)
        .increment(1)
        .build();

    val results = scenario.service.batchCreateInternal(request);
    val ids = results.values().stream().map(x -> x.id()).collect(Collectors.toSet());

    assertThat(results.keySet()).containsExactly(1, 2, 3);
    results.forEach((num, result) -> {
      val name = String.valueOf(num);
      assertThat(result.id()).isNotNull();
      assertThat(result.name()).isEqualTo(name);
      assertThat(result.email()).contains("@");
    });

    verify(scenario.deps.calendarRepoMock).batchCreate(
        argThat(x -> ids.size() == x.size()
            && ids.containsAll(x.stream().map(y -> y.id()).toList())));
    verify(scenario.deps.nylasTaskSchedulerMock).exportCalendarsToNylas(
        argThat(x -> ids.size() == x.size() && ids.containsAll(x)),
        eq(false));
  }

  @Test
  void batchCreateInternal_shouldAllowNegativeNumbering() {
    val scenario = new Scenario();
    val request = ModelBuilders.internalCalendarBatchCreateRequestWithTestData()
        .start(0)
        .end(-2)
        .increment(-1)
        .build();

    val results = scenario.service.batchCreateInternal(request);

    assertThat(results.keySet()).containsExactly(0, -1, -2);
  }

  @Test
  void batchCreateInternal_shouldDryRun() {
    val scenario = new Scenario();
    val request = ModelBuilders.internalCalendarBatchCreateRequestWithTestData()
        .isDryRun(true)
        .start(100)
        .end(100)
        .increment(1)
        .build();

    val results = scenario.service.batchCreateInternal(request);

    assertThat(results.keySet()).containsExactly(100);
    verifyNoInteractions(scenario.deps.calendarRepoMock);
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void batchCreateInternal_shouldLimitCalendars() {
    val scenario = new Scenario();
    val request = ModelBuilders.internalCalendarBatchCreateRequestWithTestData()
        .namePattern("test {n}")
        .start(1)
        .end(CalendarConstraints.INTERNAL_CALENDAR_BATCH_MAX + 1)
        .increment(1)
        .build();

    assertThatCode(() -> scenario.service.batchCreateInternal(request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining(CalendarConstraints.INTERNAL_CALENDAR_BATCH_MAX + " calendars");
  }

  @Test
  void batchCreateInternal_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = ModelBuilders.internalCalendarBatchCreateRequestWithTestData()
        .namePattern("invalid")
        .start(1)
        .end(1)
        .increment(1)
        .build();

    assertThatValidationFails(() -> scenario.service.batchCreateInternal(request))
        .hasMessageContaining(InternalCalendarBatchCreateRequest.class.getSimpleName());
  }

  @Test
  void update_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = scenario.buildUpdateRequest().timezone("invalid").build();

    assertThatValidationFails(() -> scenario.service.update(request))
        .hasMessageContaining(CalendarUpdateRequest.class.getSimpleName());
  }

  @Test
  void update_shouldThrowForCalendarInDifferentOrg() {
    val scenario = new Scenario();
    val request = scenario
        .buildUpdateRequest()
        .orgId(TestData.orgId()) // different org
        .build();

    assertThatCode(() -> scenario.service.update(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Calendar");
  }

  @Test
  void update_shouldThrowForReadOnlyCalendar() {
    val scenario = new Scenario();
    scenario.calendarBuilder.isReadOnly(true);
    val request = scenario.buildUpdateRequest().build();

    assertThatCode(() -> scenario.service.update(request))
        .isInstanceOf(ReadOnlyException.class)
        .hasMessageContaining("Calendar");
  }

  @Test
  void update_internal_shouldAllowNameAndTimezoneChange() {
    val scenario = new Scenario().withAccount(Provider.INTERNAL);
    val request = scenario.buildUpdateRequest()
        .name("changed")
        .timezone("Africa/Cairo") // changed
        .build();
    val accountId = scenario.account.orElseThrow().id();

    scenario.service.update(request);

    verify(scenario.deps.calendarRepoMock).update(argThat(x ->
        x.id().equals(request.id()) && x.timezone().equals(request.timezone())));
    verify(scenario.deps.nylasTaskSchedulerMock).exportCalendarsToNylas(
        List.of(scenario.calendarId), false);
    verify(scenario.deps.nylasTaskSchedulerMock).syncAllEvents(
        accountId, scenario.calendarId, true);
  }

  @Test
  void update_noAccount_shouldAllowNameAndTimezoneChangeAndSkipSync() {
    val scenario = new Scenario();
    val request = scenario.buildUpdateRequest()
        .name("changed")
        .timezone("Africa/Cairo") // changed
        .build();

    scenario.service.update(request);

    verify(scenario.deps.calendarRepoMock).update(argThat(x ->
        x.id().equals(request.id()) && x.timezone().equals(request.timezone())));
    verify(scenario.deps.nylasTaskSchedulerMock).exportCalendarsToNylas(
        List.of(scenario.calendarId), false);
    verify(scenario.deps.nylasTaskSchedulerMock, never()).syncAllEvents(
        any(), any(), anyBoolean());
  }

  @Test
  void update_microsoft_shouldAllowTimezoneChange() {
    val scenario = new Scenario().withAccount(Provider.MICROSOFT);
    val request = scenario.buildUpdateRequest()
        .timezone("Africa/Cairo") // changed
        .build();
    val accountId = scenario.account.orElseThrow().id();

    scenario.service.update(request);

    verify(scenario.deps.calendarRepoMock).update(argThat(x ->
        x.id().equals(request.id()) && x.timezone().equals(request.timezone())));
    verify(scenario.deps.nylasTaskSchedulerMock).syncAllEvents(
        accountId, scenario.calendarId, true);
  }

  @Test
  void update_microsoft_shouldThrowForNameChange() {
    val scenario = new Scenario().withAccount(Provider.MICROSOFT);
    val request = scenario.buildUpdateRequest()
        .name("changed")
        .timezone(scenario.calendarBuilder.build().timezone()) // keep existing
        .build();

    assertViolationExceptionForField(() -> scenario.service.update(request), "name", "microsoft");
  }

  @Test
  void update_google_shouldNotScheduleAnyTasksWhenNoChanges() {
    val scenario = new Scenario().withAccount(Provider.MICROSOFT);
    val existingValues = scenario.calendarBuilder.build();
    val request = scenario.buildUpdateRequest()
        .name(existingValues.name())
        .timezone(existingValues.timezone())
        .build();

    scenario.service.update(request);

    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void update_google_shouldThrowForNameChange() {
    val scenario = new Scenario().withAccount(Provider.GOOGLE);
    val request = scenario.buildUpdateRequest()
        .name("changed")
        .timezone(scenario.calendarBuilder.build().timezone()) // keep existing
        .build();

    assertViolationExceptionForField(() -> scenario.service.update(request), "name", "google");
  }

  @Test
  void update_google_shouldThrowForTimezoneChange() {
    val scenario = new Scenario().withAccount(Provider.GOOGLE);
    val request = scenario.buildUpdateRequest()
        .name(scenario.calendarBuilder.build().name()) // keep existing
        .timezone("Africa/Cairo") // changed
        .build();

    assertViolationExceptionForField(() -> scenario.service.update(request), "timezone", "google");
  }

  @Test
  void deleteInternal_shouldThrowForCalendarInDifferentOrg() {
    val scenario = new Scenario();

    assertThatCode(() -> scenario.service.deleteInternal(TestData.orgId(), scenario.calendarId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Calendar");
  }

  @Test
  void deleteInternal_shouldThrowForReadOnlyCalendar() {
    val scenario = new Scenario();
    scenario.calendarBuilder.isReadOnly(true);

    assertThatCode(() -> scenario.service.deleteInternal(scenario.orgId, scenario.calendarId))
        .isInstanceOf(ReadOnlyException.class)
        .hasMessageContaining("Calendar");
  }

  @Test
  void deleteInternal_internal_shouldDeleteAccountLocallyAndInNylas() {
    val scenario = new Scenario().withAccount(Provider.INTERNAL);
    val accountId = scenario.account.orElseThrow().id();

    scenario.service.deleteInternal(scenario.orgId, scenario.calendarId);

    verify(scenario.deps.accountRepoMock).delete(accountId);
    verify(scenario.deps.nylasTaskSchedulerMock).deleteAccountFromNylas(accountId);
  }

  @Test
  void deleteInternal_noAccount_shouldDeleteCalendarLocally() {
    val scenario = new Scenario();

    scenario.service.deleteInternal(scenario.orgId, scenario.calendarId);

    verify(scenario.deps.calendarRepoMock).delete(scenario.calendarId);
    verifyNoInteractions(scenario.deps.accountRepoMock);
    verifyNoInteractions(scenario.deps.nylasTaskSchedulerMock);
  }

  @Test
  void deleteInternal_microsoft_shouldThrow() {
    val scenario = new Scenario().withAccount(Provider.MICROSOFT);

    assertThatCode(() -> scenario.service.deleteInternal(scenario.orgId, scenario.calendarId))
        .isInstanceOf(IllegalOperationException.class)
        .hasMessageContaining("deleted")
        .hasMessageContaining("Microsoft");
  }

  @Test
  void deleteInternal_google_shouldThrow() {
    val scenario = new Scenario().withAccount(Provider.GOOGLE);

    assertThatCode(() -> scenario.service.deleteInternal(scenario.orgId, scenario.calendarId))
        .isInstanceOf(IllegalOperationException.class)
        .hasMessageContaining("deleted")
        .hasMessageContaining("Google");
  }

  /**
   * Helper for setting up test scenarios for the service.
   */
  private static class Scenario {
    private final OrgId orgId = TestData.orgId();
    private final CalendarId calendarId = CalendarId.create();
    private final ModelBuilders.CalendarBuilder calendarBuilder = ModelBuilders.calendar()
        .id(calendarId)
        .orgId(orgId)
        .name(TestData.uuidString())
        .timezone(TestData.timezone())
        .createdAt(TestData.instant());
    private Optional<Account> account = Optional.empty();
    private final Dependencies deps = new Dependencies(
        mock(AccountRepository.class),
        mock(CalendarRepository.class),
        ValidatorWrapperFactory.createRealInstance(),
        mock(NylasTaskScheduler.class));
    private final CalendarService service = new CalendarService(
        this.deps.accountRepoMock,
        this.deps.calendarRepoMock,
        this.deps.validatorWrapper,
        this.deps.nylasTaskSchedulerMock,
        TestData.internalCalendarsConfig());

    public Scenario() {
      when(deps.calendarRepoMock.get(any(CalendarId.class)))
          .then(inv -> Optional
              .ofNullable(inv.getArgument(0))
              .filter(calendarId::equals)
              .map(x -> calendarBuilder.build())
              .orElseThrow(() -> NotFoundException.ofClass(Calendar.class)));

      when(deps.accountRepoMock.get(any(AccountId.class)))
          .then(inv -> Optional
              .ofNullable(inv.getArgument(0))
              .flatMap(id -> account.filter(x -> id.equals(x.id())))
              .orElseThrow(() -> NotFoundException.ofClass(Account.class)));
    }

    public Scenario withAccount(Provider provider) {
      return withAccount(ModelBuilders
          .accountWithTestData()
          .orgId(orgId)
          .authMethod(provider == Provider.GOOGLE
              ? AuthMethod.GOOGLE_OAUTH
              : provider == Provider.MICROSOFT
              ? AuthMethod.MS_OAUTH_SA
              : AuthMethod.INTERNAL)
          .build());
    }

    public Scenario withAccount(Account model) {
      account = Optional.of(model);
      calendarBuilder
          .accountId(model.id())
          .externalId(TestData.calendarExternalId());
      return this;
    }

    public CalendarUpdateRequest.Builder buildUpdateRequest() {
      return CalendarUpdateRequest.builder()
          .id(calendarId)
          .orgId(orgId)
          .timezone("America/Rainy_River");
    }

    private record Dependencies(
        AccountRepository accountRepoMock,
        CalendarRepository calendarRepoMock,
        ValidatorWrapper validatorWrapper,
        NylasTaskScheduler nylasTaskSchedulerMock) {
    }
  }
}
