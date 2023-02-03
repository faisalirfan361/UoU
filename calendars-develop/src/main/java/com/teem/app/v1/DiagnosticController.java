package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.app.v1.dtos.DiagnosticRequestDto;
import com.UoU.app.v1.dtos.DiagnosticResultsDto;
import com.UoU.app.v1.dtos.IdResponse;
import com.UoU.app.v1.mapping.DiagnosticMapper;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.diagnostics.Callback;
import com.UoU.core.diagnostics.DiagnosticService;
import com.UoU.core.diagnostics.RunId;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.validation.ViolationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Authorize.Diagnostics // default, but override on each method for clarity
@RestController
@RequestMapping("/v1/diagnostics")
@AllArgsConstructor
@Slf4j
@Tag(name = "Diagnostics")
public class DiagnosticController {
  private final DiagnosticService diagnosticService;
  private final DiagnosticMapper mapper;
  private final PrincipalProvider principalProvider;

  @Authorize.Diagnostics
  @GetMapping("/sync/calendars/{calendarId}/{runId}")
  @Operation(
      summary = "Get sync diagnostics results for a calendar",
      description = Authorize.Diagnostics.DESCRIPTION
          + "This gets the results for a diagnostic run, including status and events that have "
          + "occurred. When the run is finished, the status will be **suceeded** or **failed**.\n\n"
          + "Results are short-lived, and **expiresAt** indicates when the results will expire.\n\n"
          + "Each run event contains a **data** property with various details, depending on the "
          + "event. These details can help in troubleshooting, but don't rely on anything specific "
          + "being included in **data** because the content may change over time.")
  public DiagnosticResultsDto get(@PathVariable String calendarId, @PathVariable UUID runId) {
    val results = diagnosticService.getResults(new RunId(new CalendarId(calendarId), runId));
    return mapper.toDto(results);
  }

  @Authorize.Diagnostics
  @PostMapping("/sync/calendars")
  @Operation(
      summary = "Run sync diagnostics on a calendar",
      description = Authorize.Diagnostics.DESCRIPTION
          + "This runs diagnostics on a calendar to ensure that sync with the external calendar "
          + "provider works.\n\n"
          + "**IMPORTANT:** Diagnostics is an intensive process, so avoid running many calendars "
          + "at the same time so you don't hit rate limits or cause sync issues with your external "
          + "calendar provider.\n\n"
          + "The diagnostics run may take a few minutes, so you can provide a **callbackUri** to "
          + "be notified upon completion via a POST with following JSON payload:\n\n`"
          + Callback.EXAMPLE + "`\n\n"
          + "A new diagnostic run cannot be started if another run is currently active. If another "
          + "run is active, the active run id will be returned instead of a new run id.\n\n"
          + "During diagnostics, a test event will be created on the calendar. The test event will "
          + "be removed, but there is a chance something could go wrong that prevents the event "
          + "from being removed from the external calendar provider. Therefore, you should only "
          + "run diagnostics on calendars where this risk is acceptable.")
  public IdResponse<UUID> run(@RequestBody DiagnosticRequestDto request) {
    val requestModel = mapper.toModel(request, principalProvider.current().orgId());

    try {
      return new IdResponse<>(diagnosticService.run(requestModel).id());
    } catch (NotFoundException | IllegalOperationException ex) {
      // Since calendarId is in body, treat not found and illegal op exceptions as calendarId
      // violations rather than the default handling, which for a NotFoundException would be a 404.
      throw ViolationException.forField("calendarId", ex.getMessage());
    }
  }
}
