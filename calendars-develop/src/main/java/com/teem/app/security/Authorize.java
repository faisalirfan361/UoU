package com.UoU.app.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Custom meta-annotations that authorize access to API endpoints using our {@link Scopes}.
 *
 * <p>These attributes can be added on controller classes and methods. Usually, you'll want to apply
 * a default to the class (just in case) and then override on each method for clarity.
 *
 * <p>Each attribute has a DESCRIPTION property that can be used for the API docs so that it's
 * clear what scope is required each endpoint.
 *
 * <p>Example controller method:
 * <pre>{@code
 * @Authorize.AccountsRead
 * @GetMapping
 * @Operation(
 *     summary = "Get all accounts",
 *     description = Authorize.AccountsRead.DESCRIPTION + "Some more description...")
 * public List<AccountDto> list() {}
 * }</pre>
 */
public class Authorize {

  /**
   * Prefix to use when checking scopes with expressions (because spring prefixes the scopes).
   */
  private static final String EXP_PREFIX = "SCOPE_";

  // Strings for building scope descriptions (for API docs)
  private static final String SCOPE_REQUIRED = "`— Scope required: ";
  private static final String SCOPE_REQUIRED_ONE_OF = "`— Scope required (one of): ";
  private static final String SCOPE_REQUIRED_SEP = ", ";
  private static final String SCOPE_REQUIRED_END = "`\n\n";

  /**
   * Can write and read accounts and service accounts.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAuthority('"
      + EXP_PREFIX + Scopes.ACCOUNTS + "')")
  public @interface AccountsWrite {
    // DO-MAYBE: figure out if there's a way to have this meta-annotation automatically prepend the
    // description to @Operation(description) rather than having to reference DESCRIPTION. We could
    // add our own @Operation(description) here, but then it couldn't be added on the method too.
    String DESCRIPTION = SCOPE_REQUIRED
        + Scopes.ACCOUNTS
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can read accounts and service accounts.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAnyAuthority('"
      + EXP_PREFIX + Scopes.ACCOUNTS + "','"
      + EXP_PREFIX + Scopes.ACCOUNTS_READONLY + "')")
  public @interface AccountsRead {
    String DESCRIPTION = SCOPE_REQUIRED_ONE_OF
        + Scopes.ACCOUNTS + SCOPE_REQUIRED_SEP
        + Scopes.ACCOUNTS_READONLY
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can write and read calendars for an entire org.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAuthority('"
      + EXP_PREFIX + Scopes.CALENDARS + "')")
  public @interface CalendarsWrite {
    String DESCRIPTION = SCOPE_REQUIRED
        + Scopes.CALENDARS
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can read calendars for an entire org.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAnyAuthority('"
      + EXP_PREFIX + Scopes.CALENDARS + "','"
      + EXP_PREFIX + Scopes.CALENDARS_READONLY + "')")
  public @interface CalendarsRead {
    String DESCRIPTION = SCOPE_REQUIRED_ONE_OF
        + Scopes.CALENDARS + SCOPE_REQUIRED_SEP
        + Scopes.CALENDARS_READONLY
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can write and read events for an entire org.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAuthority('"
      + EXP_PREFIX + Scopes.EVENTS + "')")
  public @interface EventsWrite {
    String DESCRIPTION = SCOPE_REQUIRED
        + Scopes.EVENTS
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can read events for an entire org.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAnyAuthority('"
      + EXP_PREFIX + Scopes.EVENTS + "','"
      + EXP_PREFIX + Scopes.EVENTS_READONLY + "')")
  public @interface EventsRead {
    String DESCRIPTION = SCOPE_REQUIRED_ONE_OF
        + Scopes.EVENTS + SCOPE_REQUIRED_SEP
        + Scopes.EVENTS_READONLY
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can write and read personal events (user is owner or participant).
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAuthority('"
      + EXP_PREFIX + Scopes.EVENTS_PERSONAL + "')")
  public @interface EventsPersonal {
    String DESCRIPTION = SCOPE_REQUIRED
        + Scopes.EVENTS_PERSONAL
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can see advanced diagnostics to troubleshoot calendar and sync issues for an entire org.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAuthority('"
      + EXP_PREFIX + Scopes.DIAGNOSTICS + "')")
  public @interface Diagnostics {
    String DESCRIPTION = SCOPE_REQUIRED
        + Scopes.DIAGNOSTICS
        + SCOPE_REQUIRED_END;
  }

  /**
   * Can perform administrative actions.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAuthority('"
      + EXP_PREFIX + Scopes.ADMIN + "')")
  public @interface Admin {
    String DESCRIPTION = SCOPE_REQUIRED
        + Scopes.ADMIN
        + SCOPE_REQUIRED_END;
  }
}
