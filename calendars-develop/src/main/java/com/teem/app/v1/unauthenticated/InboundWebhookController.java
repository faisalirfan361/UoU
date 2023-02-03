package com.UoU.app.v1.unauthenticated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU.app.security.HmacUtil;
import com.UoU.app.v1.dtos.nylas.NotificationDto;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.ValidationException;
import javax.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/inbound-webhooks")
@Slf4j
@AllArgsConstructor
@Tag(name = "Webhooks (inbound)")
public class InboundWebhookController {
  private HmacUtil hmacUtil;
  private NylasTaskScheduler nylasTaskScheduler;
  private ObjectMapper mapper;

  @PostMapping("/nylas")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Handles inbound webhooks from Nylas")
  public void push(@RequestBody String body, @RequestHeader("X-Nylas-Signature") String hmac) {
    if (!hmacUtil.validate(hmac, body)) {
      throw new ValidationException("HMAC validation failed");
    }

    NotificationDto notification;
    try {
      notification = mapper.readValue(body, NotificationDto.class);
    } catch (JsonProcessingException e) {
      throw new ValidationException("Invalid request body sent");
    }

    val info = notification.deltas().get(0);
    val accountId = new AccountId(info.objectData().accountId());
    log.debug("Nylas {} push received for account={}, {}={}, raw={}",
        info.type(), accountId.value(), info.objectData().object(), info.objectData().id(), body);

    switch (info.type()) {
      // TODO: When we create an event and exportToNylas, we then receive immediate webhooks back,
      // usually 1 EVENT_CREATED immediately and then 2 EVENT_UPDATED 10-20 seconds later. Each of
      // the webhooks has a different delta time, so they are not duplicates. These probably have
      // something to do with nylas getting changes back from the provider for normalization and
      // such. We need to figure out how to handle this to reduce unnecessary updates. For example,
      // we could at least debounce the EVENT_UPDATED events by a few seconds since they are usually
      // received less than a second apart. We need to think about it.
      case EVENT_CREATED, EVENT_UPDATED -> {
        val externalId = new EventExternalId(info.objectData().id());
        nylasTaskScheduler.importEventFromNylas(accountId, externalId);
      }
      case EVENT_DELETED -> {
        // This is for rare syncback errors. EVENT_UPDATED with cancelled status is for deletes.
        // See https://developer.nylas.com/docs/developer-tools/webhooks/#event-update-and-delete
        val externalId = new EventExternalId(info.objectData().id());
        nylasTaskScheduler.handleEventDeleteFromNylas(accountId, externalId);
      }
      case CALENDAR_CREATED, CALENDAR_UPDATED -> {
        val calendarExternalId = new CalendarExternalId(info.objectData().id());
        nylasTaskScheduler.importCalendarFromNylas(accountId, calendarExternalId, false);
      }
      case CALENDAR_DELETED -> {
        val calendarExternalId = new CalendarExternalId(info.objectData().id());
        nylasTaskScheduler.handleCalendarDeleteFromNylas(accountId, calendarExternalId);
      }
      case ACCOUNT_RUNNING, ACCOUNT_STOPPED, ACCOUNT_CONNECTED, ACCOUNT_INVALID -> {
        nylasTaskScheduler.updateAccountSyncState(accountId);
      }
      default -> {
        log.error("Unknown webhook received from Nylas: {}", info.type());
        throw new ValidationException("Unknown webhook type");
      }
    }
  }

  @GetMapping("/nylas")
  @Operation(summary = "Handles webhook verification from Nylas")
  public String verifyWebhook(@QueryParam("challenge") String challenge) {
    return challenge;
  }
}
