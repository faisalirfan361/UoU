package com.UoU.app.security;

/**
 * Scopes that can be passed in JWTs to grant authorization to things.
 */
public class Scopes {
  public static final String ACCOUNTS = "accounts";
  public static final String ACCOUNTS_READONLY = "accounts.readonly";

  public static final String CALENDARS = "calendars";
  public static final String CALENDARS_READONLY = "calendars.readonly";

  public static final String EVENTS = "events";
  public static final String EVENTS_READONLY = "events.readonly";
  public static final String EVENTS_PERSONAL = "events.personal";

  public static final String DIAGNOSTICS = "diagnostics";

  public static final String ADMIN = "admin";
}
