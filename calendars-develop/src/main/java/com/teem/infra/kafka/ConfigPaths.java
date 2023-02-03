package com.UoU.infra.kafka;

public class ConfigPaths {
  private static final String BASE = "kafka.configs";

  public static class Tasks {
    private static final String BASE = ConfigPaths.BASE + ".tasks";

    public static final String IMPORT_ALL_CALENDARS_FROM_NYLAS = BASE
        + ".import-all-calendars-from-nylas";
    public static final String EXPORT_CALENDARS_TO_NYLAS = BASE
        + ".export-calendars-to-nylas";
    public static final String CHANGE_CALENDAR = BASE + ".change-calendar";
    public static final String SYNC_ALL_EVENTS = BASE + ".sync-all-events";
    public static final String CHANGE_EVENT = BASE + ".change-event";
    public static final String DELETE_ACCOUNT_FROM_NYLAS = BASE + ".delete-account-from-nylas";
    public static final String UPDATE_ALL_SUBACCOUNT_TOKENS = BASE
        + ".update-all-subaccount-tokens";
    public static final String UPDATE_SUBACCOUNT_TOKEN = BASE + ".update-subaccount-token";
    public static final String UPDATE_ACCOUNT_SYNC_STATE = BASE + ".update-account-sync-state";
    public static final String MAINTENANCE = BASE + ".maintenance";
    public static final String DIAGNOSTICS = BASE + ".diagnostics";
  }

  public static class Events {
    private static final String BASE = ConfigPaths.BASE + ".events";

    public static final String EVENT_CHANGED = BASE + ".event-changed";
  }

  public static class PublicEvents {
    private static final String BASE = ConfigPaths.BASE + ".public-events";

    public static final String EVENT_CHANGED = BASE + ".event-changed";
  }
}
