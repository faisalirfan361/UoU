package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.app.v1.dtos.EventCreateRequestDto;
import com.UoU.app.v1.dtos.EventDto;
import com.UoU.app.v1.dtos.EventUpdateRequestDto;
import com.UoU.app.v1.dtos.IdResponse;
import com.UoU.app.v1.dtos.PageParamsDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.app.v1.dtos.WhenParamsDto;
import com.UoU.app.v1.mapping.EventMapper;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.EventConstraints;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventQuery;
import com.UoU.core.events.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Authorize.EventsWrite // default, but override on each method for clarity
@RestController
@RequestMapping("/v1/events")
@AllArgsConstructor
@Slf4j
@Tag(name = "Events")
class EventController {

  private static final String SEE_ALSO_ACTIVE_PERIOD = "**See also &rarr; "
      + "[Events active period](/docs/v1.html#active-period)**";

  private static final String SEE_ALSO_RECURRING = "**See also &rarr; "
      + "[Recurring events](/docs/v1.html#recurrence)**";

  private final EventService eventService;
  private final PrincipalProvider principalProvider;
  private final EventMapper mapper;

  @Authorize.EventsRead
  @GetMapping("/bycalendar/{calendarId}")
  @Operation(
      summary = "Get events by calendar",
      description = Authorize.EventsRead.DESCRIPTION
          + "**Active period:** Only events that start within the active period are returned. "
          + SEE_ALSO_ACTIVE_PERIOD + "\n\n"
          + "**Recurring Events:** By default, recurring events are not expanded, but series "
          + "masters and any instances that are overrides/exceptions are returned. Pass "
          + "**expandRecurring=true** to get all recurring instances (but not series masters). "
          + "In the results, recurring master events will have a **recurrence** property with the "
          + "schedule (RRULE), and instances will have a **recurrenceInstance** property that "
          + "includes the id of the master event. " + SEE_ALSO_RECURRING)
  @WhenParamsDto.ParametersInQuery
  @PageParamsDto.ParametersInQuery
  public PagedItems<EventDto> listByCalendar(
      @PathVariable("calendarId")
      String rawCalendarId,

      @RequestParam(defaultValue = "false")
      @Schema(description = "Expand recurring event instances (true) or return only masters "
          + "and overrides/exceptions (false)")
      boolean expandRecurring,

      @Parameter(hidden = true)
      WhenParamsDto when,

      @Parameter(hidden = true)
      PageParamsDto page,

      @Parameter(hidden = true)
      @RequestParam(defaultValue = "false")
      boolean includeDebugInfo) {

    val query = EventQuery.builder()
        .orgId(principalProvider.current().orgId())
        .calendarId(new CalendarId(rawCalendarId))
        .when(mapper.toWhenQueryModel(when))
        .expandRecurring(expandRecurring)
        .page(mapper.toPageParamsModel(page))
        .build();
    val pagedItems = eventService.list(query);

    return mapper.toPagedEventsDto(pagedItems, includeDebugInfo);
  }

  @Authorize.EventsRead
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Get event",
      description = Authorize.EventsRead.DESCRIPTION + "\n\n" + SEE_ALSO_RECURRING)
  public EventDto get(
      @PathVariable("id") UUID rawId,
      @Parameter(hidden = true) @RequestParam(defaultValue = "false") boolean includeDebugInfo) {
    return mapper.toEventDto(
        eventService.get(principalProvider.current().orgId(), new EventId(rawId)),
        includeDebugInfo);
  }

  @Authorize.EventsWrite
  @PostMapping
  @ResponseBody
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create an event",
      description = Authorize.EventsWrite.DESCRIPTION
          + "This endpoint creates an event and returns the new event id.\n\n"
          + "**Active period:** The event start must be within the active period. "
          + SEE_ALSO_ACTIVE_PERIOD + "\n\n"
          + "**Recurring events:** Only recurring master events can be created, not instances. "
          + "Once the master event is created, the instances will be created automatically "
          + "according to the RRULE, and then those instances can be updated by id. "
          + SEE_ALSO_RECURRING + "\n\n"
          + "**Conferencing (Microsoft Teams, Zoom):** A conferencing meeting can be automatically "
          + "created and attached to the event based on a pre-authorized conferencing user. "
          + "Conferencing users can be authorized via **/v1/auth/connect/{method}/{code}**. "
          + "The conferencing user email must match the requesting user's email. "
          + "The conferencing service used will be based on the supplied user id. The optional "
          + "language field will be passed to the conferencing service for localized join info. "
          + "**See also &rarr; [Conferencing](/docs/v1.html#conferencing)**\n\n"
          + "**dataSource:** The dataSource is arbitrary metadata you can optionally pass to "
          + "record where the event was created from (mobile, web, etc.), which will be stored "
          + "with the event and also published in any public Kafka messages. ")
  public IdResponse<UUID> create(@RequestBody EventCreateRequestDto request) {
    val id = EventId.create();
    val model = mapper.toRequestModel(request, id, principalProvider.current());

    eventService.create(model);

    log.debug("Created event: {}", id);
    return new IdResponse<>(id.value());
  }

  @Authorize.EventsWrite
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Update an event",
      description = Authorize.EventsWrite.DESCRIPTION
          + "**Active period:** The event start must be within the active period. "
          + SEE_ALSO_ACTIVE_PERIOD + "\n\n"
          + "**Read-only events:** Read-only events cannot be updated. Read-only events are "
          + "usually events that are owned by another calendar that the current account does not "
          + "have permission to edit. However, the event can always be deleted from the current "
          + "calendar, which will decline the current calendar's participation in the event.\n\n"
          + "**Recurring events:** If a recurring master event is updated, any recurring instances "
          + "will automatically be updated. If a recurring instance is updated, that instance will "
          + "become an override/exception. " + SEE_ALSO_RECURRING + "\n\n"
          + "**dataSource:** The dataSource is arbitrary metadata you can optionally pass to "
          + "record where the event was updated from (mobile, web, etc.), which will be stored "
          + "with the event and also published in any public Kafka messages. ")
  public void update(@PathVariable("id") UUID rawId, @RequestBody EventUpdateRequestDto request) {
    val id = new EventId(rawId);
    eventService.update(mapper.toRequestModel(request, id, principalProvider.current().orgId()));
    log.debug("Updated event: {}", id);
  }

  @Authorize.EventsWrite
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete an event",
      description = Authorize.EventsWrite.DESCRIPTION
          + "**Read-only events:** Read-only events can be deleted. But since the associated "
          + "calendar does not own the event, deleting the event will only decline the calendar's "
          + "participation in the event (without affecting the original event on the owner's "
          + "calendar).\n\n"
          + "**Recurring events:** If a recurring master is deleted, the entire series will be "
          + "deleted. If a recurring instance is deleted, the behavior is slightly different "
          + "depending on provider. For Microsoft, the single instance will be deleted, and an "
          + "EXDATE will be added to the master event. For Google, the single instance will remain "
          + "but show a cancelled status, and no EXDATE will be added to the master event. "
          // DO-LATER: When we integrate Google, we'll need to change a few things to get the above
          // described behavior to work, like keeping the event and exposing the cancelled status.
          // See Nylas docs for details about the MS/Google recurrence differences.
          + SEE_ALSO_RECURRING + "\n\n"
          + "**dataSource:** The dataSource is arbitrary metadata you can optionally pass to "
          + "record where the event was deleted from (mobile, web, etc.), which will be published "
          + "in any public Kafka messages. ")
  public void delete(
      @PathVariable("id")
      UUID rawId,

      @RequestParam(required = false)
      @Schema(maxLength = EventConstraints.DATA_SOURCE_API_MAX, example = "mobile")
      String dataSource) {

    val request = mapper.toRequestModel(
        new EventId(rawId), principalProvider.current().orgId(), dataSource);
    eventService.delete(request);
    log.debug("Deleted event: {}", rawId);
  }

  @Authorize.EventsWrite
  @PostMapping("/{id}/checkin")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Checkin an event",
      description = Authorize.EventsWrite.DESCRIPTION
          + "**Read-only events:** Read-only events do allow checkin.\n\n"
          + "**dataSource:** The dataSource is arbitrary metadata you can optionally pass to "
          + "record where the event checkin originated (mobile, web, etc.), which will be stored "
          + "with the event and also published in any public Kafka messages. ")
  public void checkin(
      @PathVariable("id")
      UUID rawId,

      @RequestParam(required = false)
      @Schema(maxLength = EventConstraints.DATA_SOURCE_API_MAX, example = "mobile")
      String dataSource) {

    val request = mapper.toRequestModel(
        new EventId(rawId), principalProvider.current().orgId(), dataSource);
    eventService.checkin(request);
  }

  @Authorize.EventsWrite
  @PostMapping("/{id}/checkout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Checkout an event",
      description = Authorize.EventsWrite.DESCRIPTION
          + "**Read-only events:** Read-only events do allow checkout.\n\n"
          + "**dataSource:** The dataSource is arbitrary metadata you can optionally pass to "
          + "record where the event checkout originated (mobile, web, etc.), which will be stored "
          + "with the event and also published in any public Kafka messages. ")
  public void checkout(
      @PathVariable("id")
      UUID rawId,

      @RequestParam(required = false)
      @Schema(maxLength = EventConstraints.DATA_SOURCE_API_MAX, example = "mobile")
      String dataSource) {

    val request = mapper.toRequestModel(
        new EventId(rawId), principalProvider.current().orgId(), dataSource);
    eventService.checkout(request);
  }
}
