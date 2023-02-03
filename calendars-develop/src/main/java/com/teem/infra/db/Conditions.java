package com.UoU.infra.db;

import com.UoU.core.OrgId;
import com.UoU.infra.jooq.tables.Account;
import com.UoU.infra.jooq.tables.Calendar;
import com.UoU.infra.jooq.tables.Event;
import com.UoU.infra.jooq.tables.ServiceAccount;
import org.jooq.Condition;

/**
 * Helper for creating common conditions in a standard way to prevent accidents.
 */
class Conditions {
  public static Condition orgMatches(ServiceAccount table, OrgId orgId) {
    return table.ORG_ID.eq(orgId.value());
  }

  public static Condition orgMatches(Account table, OrgId orgId) {
    return table.ORG_ID.eq(orgId.value());
  }

  public static Condition orgMatches(Calendar table, OrgId orgId) {
    return table.ORG_ID.eq(orgId.value());
  }

  public static Condition orgMatches(Event table, OrgId orgId) {
    return table.ORG_ID.eq(orgId.value());
  }
}
