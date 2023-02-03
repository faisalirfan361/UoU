package com.UoU.core.conferencing;

import com.UoU.core.accounts.Provider;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.calendars.ZoneParserSupplier;
import com.UoU.core.conferencing.teams.TeamsService;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.validation.ViolationException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Provider-agnostic service for working with conferencing meetings (Teams, Zoom).
 *
 * <p>The conferencing provider is chosen based on a conferencing user's auth method, but the point
 * of this service is that it will figure out which conferencing provider to use and handle
 * delegating calls to provider-specific code for you.
 */
@Service
@AllArgsConstructor
public class ConferencingService {
  private final CalendarRepository calendarRepo;
  private final ConferencingUserRepository conferencingUserRepo;
  private final TeamsService teamsService;

  /**
   * Adds conferencing info to the event description and returns a new event create request.
   *
   * <p>The conferencing provider (Teams, Zoom) is based on the conferencing user's auth method.
   */
  public EventCreateRequest addConferencingToEvent(EventCreateRequest request) {
    // Conferencing was not requested, so return original request.
    if (request == null || request.conferencing() == null) {
      return request;
    }

    val user = Optional
        .ofNullable(request.conferencing().userId())
        .flatMap(id -> NotFoundException.catchToOptional(() -> conferencingUserRepo.get(id)))
        .filter(x -> x.orgId().equals(request.orgId()))
        .orElseThrow(() -> ViolationException.forField(
            "conferencing.autoCreate.userId", "Conferencing user not found"));

    // For now, we require the requesting principal email to match the conferencing user email.
    // This is pretty limited, but we need to figure out our permission model later to support
    // things like email aliases, different emails, globally shared conferencing users, etc.
    if (!user.email().equalsIgnoreCase(request.conferencing().principalEmail())) {
      throw ViolationException.forField(
          "conferencing.autoCreate.userId", "Conferencing user email is invalid for request user");
    }

    // Try to get locale from language tag, else pass null so user default gets used.
    val locale = Optional.ofNullable(request.conferencing().language())
        .filter(x -> !x.isBlank())
        .map(Locale::forLanguageTag)
        .orElse(null);

    // Based on auth method, delegate handling to a provider service.
    return switch (user.authMethod()) {
      case CONF_TEAMS_OAUTH -> teamsService.addConferencingToEvent(
          request,
          user.id(),
          createCalendarTimeZoneSupplier(request.calendarId()),
          getAccountProvider(request.calendarId()),
          locale);

      default -> throw new IllegalStateException(
          "Conferencing user auth method is invalid: " + user.authMethod().getValue());
    };
  }

  /**
   * Fetches the account provider for the calendar, or INTERNAL when no account exists.
   *
   * <p>If the account doesn't exist (yet), this will return INTERNAL since internal calendars are
   * the only scenario where calendars are created before accounts. This should be rare.
   */
  private Provider getAccountProvider(CalendarId calendarId) {
    return calendarRepo.getAccountProvider(calendarId).orElse(Provider.INTERNAL);
  }

  /**
   * Creates a timezone supplier that fetches the calendar timezone and parses it into a ZoneId.
   */
  private Supplier<ZoneId> createCalendarTimeZoneSupplier(CalendarId calendarId) {
    return new ZoneParserSupplier(() -> calendarRepo.getTimezone(calendarId), true);
  }
}
