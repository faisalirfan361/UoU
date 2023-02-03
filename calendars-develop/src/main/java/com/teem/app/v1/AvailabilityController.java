package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.app.v1.dtos.AvailabilityRequestDto;
import com.UoU.app.v1.dtos.AvailabilityResponseDto;
import com.UoU.app.v1.dtos.FreeBusyDetailedResponseDto;
import com.UoU.app.v1.dtos.FreeBusyResponseDto;
import com.UoU.app.v1.mapping.AvailabilityMapper;
import com.UoU.core.DataConfig;
import com.UoU.core.calendars.AvailabilityRequest;
import com.UoU.core.calendars.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Authorize.CalendarsRead // default, but override on each method for clarity
@RestController
@RequestMapping("/v1/calendars")
@AllArgsConstructor
@Tag(name = "Availability")
public class AvailabilityController {
  private static final String COMMON_DESCRIPTION = "Note that start times are inclusive and end "
      + "times are exclusive, so events can abut the request timespan without making the calendar "
      + "unavailable.\n\n"
      + "You can check a maximum of **" + DataConfig.Availability.MAX_CALENDARS + " calendarIds** "
      + "for a maximum timespan of **" + DataConfig.Availability.MAX_DURATION_DAYS + " days**.";

  private final AvailabilityService availabilityService;
  private final PrincipalProvider principalProvider;
  private final AvailabilityMapper mapper;

  @Authorize.CalendarsRead
  @PostMapping("/availability")
  @Operation(
      summary = "Get calendar availability (true/false)",
      description = Authorize.CalendarsRead.DESCRIPTION
          + "This returns true/false availability for the passed calendar ids. If you need more "
          + "info, like the actual busy periods, use **/freebusy** instead.\n\n"
          + COMMON_DESCRIPTION)
  public AvailabilityResponseDto getAvailability(@RequestBody AvailabilityRequestDto request) {
    var result = availabilityService.getAvailability(mapRequest(request));
    return mapper.toAvailabilityResponseDto(result);
  }

  /* DO-MAYBE: Add detailed availability for UoU use cases. Something like:
  @Authorize.CalendarsRead
  @GetMapping("/availability/detailed")
  @Operation(
      summary = "Get calendar availability with extra details about current or next availability ",
      description = Authorize.CalendarsRead.DESCRIPTION
          + "This works like `/availability` but provides some extra details about the current or "
          + "next availability period. If you don't need the extra details, use `/availability` "
          + "instead.")
  public AvailabilityDetailedResponseDto getAvailabilityDetailed() {
    // Response:
    // {
    //   "itemsById": {
    //     "abc": {
    //       "isAvailable": true,
    //       "currentAvailability": {
    //         "start": "2022-01-01T15:00:00Z",
    //         "end": "2022-01-01T17:00:00Z"
    //       }
    //     },
    //     "def": {
    //       "isAvailable": false,
    //       "nextAvailability": {
    //         "start": "2022-01-01T17:00:00Z",
    //         "end": "2022-01-01T18:00:00Z"
    //       }
    //     },
    //   }
    // }
  }
  */

  @Authorize.CalendarsRead
  @PostMapping("/freebusy")
  @Operation(
      summary = "Get calendar busy time periods",
      description = Authorize.CalendarsRead.DESCRIPTION
          + "This returns the busy time periods for the passed calendar ids. If a calendar has "
          + "multiple overlapping events, each will be returned as a separate busy time period; "
          + "however, time periods that are exact duplicates will be excluded.\n\n"
          + COMMON_DESCRIPTION)
  public FreeBusyResponseDto getFreeBusy(@RequestBody AvailabilityRequestDto request) {
    var result = availabilityService.getBusyPeriods(mapRequest(request));
    return mapper.toFreeBusyResponseDto(result);
  }

  @Authorize.CalendarsRead
  @PostMapping("/freebusy/detailed")
  @Operation(
      summary = "Get calendar busy time periods with some extra event details",
      description = Authorize.CalendarsRead.DESCRIPTION
          + "This works like `/freebusy` but provides some extra event details for each time "
          + "period. If you don't need the extra details, use **/freebusy** instead.\n\n"
          + COMMON_DESCRIPTION)
  public FreeBusyDetailedResponseDto getFreeBusyDetailed(
      @RequestBody AvailabilityRequestDto request) {
    var result = availabilityService.getDetailedBusyPeriods(mapRequest(request));
    return mapper.toFreeBusyDetailedResponseDto(result);
  }

  private AvailabilityRequest mapRequest(AvailabilityRequestDto request) {
    return mapper.toAvailabilityRequest(request, principalProvider.current().orgId());
  }
}
