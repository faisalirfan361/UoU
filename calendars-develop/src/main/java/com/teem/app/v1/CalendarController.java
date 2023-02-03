package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.app.v1.dtos.CalendarDto;
import com.UoU.app.v1.dtos.CalendarUpdateRequestDto;
import com.UoU.app.v1.dtos.InternalCalendarBatchCreateRequestDto;
import com.UoU.app.v1.dtos.InternalCalendarBatchInfoDto;
import com.UoU.app.v1.dtos.InternalCalendarCreateRequestDto;
import com.UoU.app.v1.dtos.InternalCalendarInfoDto;
import com.UoU.app.v1.dtos.PageParamsDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.app.v1.mapping.CalendarMapper;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Authorize.CalendarsWrite // default, but override on each method for clarity
@RestController
@RequestMapping("/v1/calendars")
@AllArgsConstructor
@Slf4j
@Tag(name = "Calendars")
public class CalendarController {

  private static final String INTERNAL_CALENDARS_ONLY_DESC = """
      **For internal calendars only:** Google and Microsoft calendars must be created and modified
      directly in the provider, and changes will be synced to the Calendars API (if the account is
      currently authorized).

      <a href="/docs/v1.html#internal-calendars">See more about internal calendars</a>
      """;

  private final PrincipalProvider principalProvider;
  private final CalendarService calendarService;
  private final CalendarMapper mapper;

  @Authorize.CalendarsRead
  @GetMapping("/byaccount/{accountId}")
  @Operation(
      summary = "Get calendars by account",
      description = Authorize.CalendarsRead.DESCRIPTION)
  @PageParamsDto.ParametersInQuery
  public PagedItems<CalendarDto> listByAccount(
      @PathVariable String accountId,
      @Parameter(hidden = true) PageParamsDto page) {

    // Exclude read-only calendars from this list because we don't currently sync them, and it just
    // causes confusion because Nylas auto-creates read-only calendars like "Emailed events". A user
    // could get the read-only calendar by id if they knew the id, and that's fine, but we generally
    // hide it from lists and don't do any sync operations on it.
    var includeReadOnly = false;
    var pagedItems = calendarService
        .listByAccount(
            principalProvider.current().orgId(),
            new AccountId(accountId),
            includeReadOnly,
            mapper.toPageParamsModel(page));

    return mapper.toPagedCalendarsDto(pagedItems);
  }

  @Authorize.CalendarsRead
  @GetMapping("/{id}")
  @Operation(summary = "Get calendar by id", description = Authorize.CalendarsRead.DESCRIPTION)
  public CalendarDto getById(@PathVariable String id) {
    return mapper.toCalendarDto(
        calendarService.get(principalProvider.current().orgId(), new CalendarId(id)));
  }

  @Authorize.CalendarsWrite
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Update a calendar",
      description = Authorize.CalendarsWrite.DESCRIPTION + """
          Allows updating a calendar. Depending on the calendar type (Google, Microsoft, internal),
          some properties cannot be changed with this endpoint. However, you can always pass all
          properties for a calendar, and a validation error will only occur when changing a field
          that is not allowed to change. See the rules for specific calendar types below.

          **Google calendars:** Google calendars cannot be changed with this endpoint. Changes must
          be made directly in Google, and then the changes will automatically sync to Calendars API.

          **Microsoft calendars:** Microsoft calendars allow only the timezone to be changed with
          this endpoint (other properties must be changed directly in Microsoft). Until set,
          Microsoft calendars will use the default timezone. The effective timezone can be fetched
          via **GET /v1/calendars/{id}**

          **Internal calendars:** Internal calendars allow the name and timezone properties to be
          changed with this endpoint.

          **Calendar timezone:** The calendar timezone is used to interpret all-day event start and
          end times when exact points in time are needed, such as for the availability endpoints,
          since all-day events do not represent exact points in time.
          """)
  public void update(
      @PathVariable("id") String rawId, @RequestBody CalendarUpdateRequestDto request) {

    val id = new CalendarId(rawId);
    calendarService.update(
        mapper.toUpdateRequestModel(request, id, principalProvider.current().orgId()));
  }

  @Authorize.CalendarsWrite
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create an internal calendar that DOES NOT sync to Google or Microsoft",
      description = Authorize.AccountsWrite.DESCRIPTION + INTERNAL_CALENDARS_ONLY_DESC)
  public InternalCalendarInfoDto createInternal(
      @RequestBody InternalCalendarCreateRequestDto request) {

    val result = calendarService.createInternal(
        mapper.toInternalCalendarCreateRequestModel(request, principalProvider.current().orgId()));

    return mapper.toInternalCalendarInfoDto(result);
  }

  @Authorize.CalendarsWrite
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create a batch of internal calendars that DO NOT sync to Google or Microsoft",
      description = Authorize.AccountsWrite.DESCRIPTION
          + INTERNAL_CALENDARS_ONLY_DESC + "\n\n" + """
          **Batch info:** This allows you to create a batch of internal calendars all at once. You
          must specify a **namePattern** like "Room {n}", where **{n}** will be replaced with the
          batch item number. Specify the **start**, **end**, and **increment** to determine
          the numbering behavior.

          **Dry run:** You can set the **dryRun** property to see the batch of internal calendars
          that would be created without actually creating anything yet.
          """)
  public InternalCalendarBatchInfoDto batchCreateInternal(
      @RequestBody InternalCalendarBatchCreateRequestDto request) {

    val results = calendarService.batchCreateInternal(
        mapper.toInternalCalendarBatchCreateRequestModel(
            request, principalProvider.current().orgId()));

    return mapper.toInternalCalendarBatchInfoDto(results);
  }

  @Authorize.CalendarsWrite
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete an internal calendar",
      description = Authorize.AccountsWrite.DESCRIPTION + INTERNAL_CALENDARS_ONLY_DESC)
  public void deleteInternal(@PathVariable String id) {
    calendarService.deleteInternal(principalProvider.current().orgId(), new CalendarId(id));
  }
}
