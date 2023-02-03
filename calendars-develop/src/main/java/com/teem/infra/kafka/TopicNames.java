package com.UoU.infra.kafka;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kafka topic names pulled from app configuration.
 */
public class TopicNames {

  @Component
  @Getter
  public static class Tasks {
    private final String importAllCalendarsFromNylas;
    private final String exportCalendarsToNylas;
    private final String changeCalendar;
    private final String syncAllEvents;
    private final String changeEvent;
    private final String deleteAccountFromNylas;
    private final String updateAllSubaccountTokens;
    private final String updateSubaccountToken;
    private final String updateAccountSyncState;
    private final String maintenance;
    private final String diagnostics;

    public Tasks(
        @Value("${" + ConfigPaths.Tasks.IMPORT_ALL_CALENDARS_FROM_NYLAS + ".topic.name}")
        String importAllCalendarsFromNylas,

        @Value("${" + ConfigPaths.Tasks.EXPORT_CALENDARS_TO_NYLAS + ".topic.name}")
        String exportCalendarsToNylas,

        @Value("${" + ConfigPaths.Tasks.SYNC_ALL_EVENTS + ".topic.name}")
        String syncAllEvents,

        @Value("${" + ConfigPaths.Tasks.DELETE_ACCOUNT_FROM_NYLAS + ".topic.name}")
        String deleteAccountFromNylas,

        @Value("${" + ConfigPaths.Tasks.CHANGE_CALENDAR + ".topic.name}")
        String changeCalendar,

        @Value("${" + ConfigPaths.Tasks.CHANGE_EVENT + ".topic.name}")
        String changeEvent,

        @Value("${" + ConfigPaths.Tasks.UPDATE_ALL_SUBACCOUNT_TOKENS + ".topic.name}")
        String updateAllSubaccountTokens,

        @Value("${" + ConfigPaths.Tasks.UPDATE_SUBACCOUNT_TOKEN + ".topic.name}")
        String updateSubaccountToken,

        @Value("${" + ConfigPaths.Tasks.UPDATE_ACCOUNT_SYNC_STATE + ".topic.name}")
        String updateAccountSyncState,

        @Value("${" + ConfigPaths.Tasks.MAINTENANCE + ".topic.name}")
        String maintenance,

        @Value("${" + ConfigPaths.Tasks.DIAGNOSTICS + ".topic.name}")
        String diagnostics
    ) {
      this.importAllCalendarsFromNylas = importAllCalendarsFromNylas;
      this.exportCalendarsToNylas = exportCalendarsToNylas;
      this.syncAllEvents = syncAllEvents;
      this.deleteAccountFromNylas = deleteAccountFromNylas;
      this.changeCalendar = changeCalendar;
      this.changeEvent = changeEvent;
      this.updateAllSubaccountTokens = updateAllSubaccountTokens;
      this.updateSubaccountToken = updateSubaccountToken;
      this.updateAccountSyncState = updateAccountSyncState;
      this.maintenance = maintenance;
      this.diagnostics = diagnostics;
    }
  }

  @Component
  @Getter
  public static class Events {
    private final String eventChanged;

    public Events(
        @Value("${" + ConfigPaths.Events.EVENT_CHANGED + ".topic.name}")
        String eventChanged
    ) {
      this.eventChanged = eventChanged;
    }
  }

  @Component
  @Getter
  public static class PublicEvents {
    private final String eventChanged;

    public PublicEvents(
        @Value("${" + ConfigPaths.PublicEvents.EVENT_CHANGED + ".topic.name}")
        String eventChanged
    ) {
      this.eventChanged = eventChanged;
    }
  }
}
