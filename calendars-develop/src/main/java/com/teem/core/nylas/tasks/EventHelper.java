package com.UoU.core.nylas.tasks;

import com.nylas.NylasAccount;
import com.nylas.RequestFailedException;
import com.UoU.core.TimeSpan;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventsConfig;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.NylasClientFactory;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Helper for common things needed in Nylas event-related tasks.
 */
@Service
@AllArgsConstructor
public class EventHelper {
  private final NylasClientFactory nylasClientFactory;
  private final AccountRepository accountRepo;
  private final CalendarRepository calendarRepo;
  private final EventsConfig eventsConfig;

  public NylasAccount createNylasClient(AccountId accountId) {
    val token = accountRepo.getAccessToken(accountId);
    return nylasClientFactory.createAccountClient(token);
  }

  public TimeSpan getCurrentActivePeriod() {
    return eventsConfig.activePeriod().current();
  }

  public Calendar getCalendarByExternalId(CalendarExternalId calendarExternalId) {
    return calendarRepo.tryGetByExternalId(calendarExternalId).orElseThrow(() ->
        NotFoundException.ofClass(Calendar.class));
  }

  public CalendarExternalId getCalendarExternalId(CalendarId calendarId) {
    return calendarRepo.tryGetExternalId(calendarId).orElseThrow(() ->
        new IllegalStateException("Calendar is missing external id: " + calendarId.value()));
  }

  /**
   * Gets the Nylas event by id if it exists, returning empty on 404.
   */
  @SneakyThrows
  public Optional<com.nylas.Event> tryGetNylasEvent(
      NylasAccount client, EventExternalId externalId) {

    try {
      return Optional.ofNullable(client.events().get(externalId.value()));
    } catch (RequestFailedException ex) {
      if (Exceptions.isNotFound(ex)) {
        return Optional.empty();
      }
      throw ex;
    }
  }
}
