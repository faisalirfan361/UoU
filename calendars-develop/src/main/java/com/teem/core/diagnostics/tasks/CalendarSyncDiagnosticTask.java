package com.UoU.core.diagnostics.tasks;

import com.nylas.FreeBusyCalendars;
import com.nylas.FreeBusyQuery;
import com.UoU.core.Fluent;
import com.UoU.core.Noop;
import com.UoU.core.OrgId;
import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.diagnostics.Callback;
import com.UoU.core.diagnostics.Config;
import com.UoU.core.diagnostics.DiagnosticRepository;
import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.SaveRequest;
import com.UoU.core.diagnostics.Status;
import com.UoU.core.diagnostics.events.AccountEvent;
import com.UoU.core.diagnostics.events.CalendarEvent;
import com.UoU.core.diagnostics.events.RunEvent;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventRepository;
import com.UoU.core.events.When;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.tasks.DeleteEventFromNylasTask;
import com.UoU.core.nylas.tasks.ExportEventToNylasTask;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.CompletableToListenableFutureAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

/**
 * Runs calendar sync diagnostics using a previously created run id.
 */
@Service
@AllArgsConstructor
@Slf4j
public class CalendarSyncDiagnosticTask implements Task<CalendarSyncDiagnosticTask.Params> {

  private final Config config;
  private final DiagnosticRepository diagnosticRepo;
  private final AccountRepository accountRepo;
  private final CalendarRepository calendarRepo;
  private final EventRepository eventRepo;
  private final ExportEventToNylasTask exportEventToNylasTask;
  private final DeleteEventFromNylasTask deleteEventFromNylasTask;
  private final NylasClientFactory nylasClientFactory;
  private final RunnerFactory runnerFactory;
  private final RestTemplate restTemplate;

  public record Params(
      RunId runId,
      String callbackUri
  ) {
    public Params(RunId runId) {
      this(runId, null);
    }
  }

  @Override
  public void run(Params params) {
    val calendar = calendarRepo.get(params.runId.calendarId());

    // Eligibility should be checked before scheduling the task, but just in case:
    calendar.requireIsEligibleToSync();

    new Workflow(runnerFactory, params.runId(), calendar, params.callbackUri()).start();
  }

  /**
   * Helper class to run the diagnostic process and keep some state along the way.
   */
  private class Workflow {
    private final Runner runner;
    private final RunId runId;
    private final CalendarExternalId calendarExternalId;
    private final AccountId accountId;
    private final OrgId orgId;
    private final String callbackUri;
    private Optional<EventId> eventId = Optional.empty();
    private Optional<EventExternalId> eventExternalId = Optional.empty();

    public Workflow(
        RunnerFactory runnerFactory,
        RunId runId,
        Calendar calendar,
        String callbackUri) {
      this.runId = runId;
      this.calendarExternalId = calendar.externalId();
      this.accountId = calendar.accountId();
      this.orgId = calendar.orgId();
      this.callbackUri = callbackUri;

      // Create a code runner for our specific runId that calls fail() on any exception.
      this.runner = runnerFactory.create(runId, this::fail);
    }

    public void start() {
      // Ensure run can only be started once while pending.
      val status = diagnosticRepo.getStatus(runId);
      if (status != Status.PENDING) {
        throw new IllegalStateException(
            "Workflow must be pending to start processing, but was " + status + ".");
      }

      // Mark run as processing.
      save(x -> x
          .status(Status.PROCESSING)
          .startedAt(Instant.now())
          .newEvent(new RunEvent.RunStarted()));

      // Do actual diagnostics.
      // From here on, catch any runner RunException, and just log it, rather than letting it bubble
      // and fail the task. RunExceptions are part of the normal diagnostic process and should be
      // reported in the diagnostic results; therefore, they shouldn't cause this task to fail.
      try {
        // Do all actions that can be done synchronously, including exporting a test event to Nylas.
        checkAccountErrors();
        fetchExternalAccount();
        verifyAccountAuth();
        createLocalEvent();
        exportEvent();

        // Now, we have to wait for the event to sync down from Provider -> Nylas -> Us.
        // Note that we *do not* call `syncFuture.get()` to await the future on purpose: we're
        // using spring task scheduler and can let the processing continue in the background.
        val syncFuture = waitForEventToSyncFromProvider();
        syncFuture.addCallback(
            x -> success(),
            ex -> Noop.because("fail() will already be triggered on any runner exception"));
      } catch (Runner.RunException ex) {
        Noop.because("runner will handle RunExceptions");
      }
    }

    /**
     * Persists run info.
     */
    private void save(Consumer<SaveRequest.Builder> build) {
      diagnosticRepo.save(Fluent
          .of(SaveRequest.builder())
          .also(build)
          .get()
          .runId(runId)
          .build());
    }

    /**
     * Checks if we have any account errors stored locally for the account.
     */
    private void checkAccountErrors() {
      val errors = runner.run(
          "Check for local account errors.",
          () -> accountRepo.listErrors(accountId, false).toList());

      save(x -> x.newEvent(
          errors.isEmpty()
              ? new AccountEvent.AccountErrorsChecked(accountId)
              : new AccountEvent.AccountErrorsChecked(accountId, errors.size(), errors.get(0))));
    }

    /**
     * Fetches the account info from Nylas to ensure our account id is valid.
     */
    private void fetchExternalAccount() {
      val nylasAccount = runner.run(
          "Fetch account info from external provider.",
          () -> {
            val client = nylasClientFactory.createApplicationClient();
            try {
              return client.accounts().get(accountId.value());
            } catch (Exception ex) {
              throw new NylasRequestFailed(ex);
            }
          });

      save(x -> x.newEvent(
          new AccountEvent.AccountFetchedExternal(nylasAccount)));
    }

    /**
     * Verifies our account access token is valid by calling the Nylas freebusy endpoint.
     */
    private void verifyAccountAuth() {
      val account = runner.run(
          "Fetch local account.",
          () -> accountRepo.get(accountId));
      val accessToken = runner.run(
          "Fetch account auth info.",
          () -> accountRepo.getAccessToken(accountId));

      val client = nylasClientFactory.createAccountClient(accessToken);
      val query = new FreeBusyQuery()
          .calendars(new FreeBusyCalendars(
              accountId.value(),
              List.of(calendarExternalId.value())))
          .startTime(Instant.now())
          .endTime(Instant.now().plusSeconds(300));

      runner.run(
          "Verify account auth via external free/busy check.",
          () -> {
            try {
              val result = client.calendars().checkFreeBusy(query);
              log.debug("Nylas checkFreeBusy succeeded with size {}", result.size());
            } catch (Exception ex) {
              throw new NylasRequestFailed(ex);
            }
          });

      save(x -> x.newEvent(
          new AccountEvent.AccountAuthVerified(account)));
    }

    /**
     * Creates a local event (not exported to Nylas yet).
     */
    private void createLocalEvent() {
      val newEventId = EventId.create();
      val start = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES);
      val eventCreateRequest = EventCreateRequest.builder()
          .id(newEventId)
          .calendarId(runId.calendarId())
          .orgId(orgId)
          .title("** Diagnostic Test " + runId.id() + " **")
          .description(
              "This event was created by request for calendar diagnostics. "
                  + "It should be removed automatically within a few minutes.")
          .when(new When.TimeSpan(start, start.plus(1, ChronoUnit.MINUTES)))
          .isBusy(false)
          .build();

      runner.run(
          "Create local event.",
          () -> eventRepo.create(eventCreateRequest));

      eventId = Optional.of(newEventId);

      save(x -> x.newEvent(
          new CalendarEvent.EventCreated(newEventId, eventCreateRequest.title(), start)));
    }

    /**
     * Exports the previously created local event to Nylas.
     */
    private void exportEvent() {
      eventId.orElseThrow(() -> new IllegalStateException("Event id must be set."));

      runner.run(
          "Export local event to begin sync to external calendar provider.",
          () -> exportEventToNylasTask.run(
              new ExportEventToNylasTask.Params(accountId, eventId.orElseThrow())));

      eventExternalId = Optional.of(
          runner.run(
              "Fetch local event after export.",
              () -> eventRepo.getExternalId(eventId.orElseThrow()).orElseThrow()));

      save(x -> x.newEvent(
          new CalendarEvent.EventExported(
              eventId.orElseThrow(), eventExternalId.orElseThrow())));
    }

    /**
     * Waits for the exported event to sync from Provider -> Nylas -> to Us.
     */
    private ListenableFuture<Void> waitForEventToSyncFromProvider() {
      eventId.orElseThrow(() -> new IllegalStateException("Event id must be set."));
      eventExternalId.orElseThrow(() -> new IllegalStateException("Event externalId must be set."));

      // Nylas does not tell us explicitly that the provider event has synced, but we can infer
      // that sync is done by checking that the icaluid is populated from the provider. Currently,
      // we can expect sync to occur from Microsoft in ~30 seconds (Google times are TBD), but
      // we should keep an eye on average times and possibly tweak later on if needed.
      val future = runner.runAsyncUntilMatch(
          "Wait for event to sync from external calendar provider.",
          () -> eventRepo.getIcalUid(eventId.orElseThrow()),
          icalUid -> icalUid.isPresent(),
          config.providerSyncWait().attempts(),
          config.providerSyncWait().delay());

      return new CompletableToListenableFutureAdapter<>(future
          .completable()
          .thenAccept(icalUid -> save(x -> x.newEvent(
              new CalendarEvent.EventSyncedFromProvider(
                  eventId.orElseThrow(), eventExternalId.orElseThrow(), icalUid.orElseThrow())))));
    }

    /**
     * Marks the run as completed with SUCCEEDED status and finishes up.
     */
    private void success() {
      try {
        deleteEvent();
      } finally {
        save(x -> x
            .status(Status.SUCCEEDED)
            .finishedAt(Instant.now())
            .newEvent(new RunEvent.RunSucceeded()));

        sendCallback(Status.SUCCEEDED);
      }
    }

    /**
     * Marks the run as failed with FAILED status and finishes up.
     *
     * <p>If the run is already finished, the error will still be recorded.
     */
    private void fail(RunEvent.ErrorOccurred errorEvent, Throwable throwable) {
      val status = diagnosticRepo.getStatus(runId);

      // If run is finished, we can't finish again, so just record error event.
      if (status.isTerminal()) {
        save(x -> x.newEvent(errorEvent));
        return;
      }

      save(x -> x
          .status(Status.FAILED)
          .finishedAt(Instant.now())
          .newEvent(errorEvent));

      try {
        deleteEvent();
      } finally {
        sendCallback(Status.FAILED);
      }
    }

    /**
     * Deletes the event from local and Nylas if needed.
     */
    private void deleteEvent() {
      if (eventId.isEmpty()) {
        return;
      }

      runner.run(
          "Delete local event.",
          () -> eventRepo.delete(eventId.orElseThrow()));

      eventExternalId.ifPresent(x -> runner.run(
          "Request delete from external calendar provider.",
          () -> deleteEventFromNylasTask.run(
              new DeleteEventFromNylasTask.Params(accountId, x))));

      save(x -> x.newEvent(
          new CalendarEvent.EventDeleted(eventId.orElseThrow())));
    }

    /**
     * Notifies the user callback that diagnostics is done, if a callback URI was specified.
     */
    private void sendCallback(Status status) {
      if (callbackUri == null || this.callbackUri.isBlank()) {
        return;
      }

      val body = new Callback("Calendar sync diagnostics finished.", runId, status);
      runner.run(
          "Notify callback URI provided by user.",
          () -> restTemplate.postForLocation(callbackUri, body, Void.class));
    }
  }

  /**
   * Wraps a Nylas exception to indicate any Nylas request failure (Nylas exceptions are checked).
   */
  private static class NylasRequestFailed extends RuntimeException {
    public NylasRequestFailed(Throwable cause) {
      super("Nylas request failed", cause);
    }
  }
}
