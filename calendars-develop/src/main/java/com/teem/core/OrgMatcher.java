package com.UoU.core;

import com.UoU.core.exceptions.NotFoundException;
import java.util.function.Supplier;

/**
 * Helper to ensure OrgId values match as expected.
 */
public class OrgMatcher {
  public static void matchOrThrow(
      OrgId orgId1, OrgId orgId2, Supplier<RuntimeException> throwable) {
    if (!orgId1.value().equals(orgId2.value())) {
      throw throwable.get();
    }
  }

  public static void matchOrThrowNotFound(OrgId orgId1, OrgId orgId2, Class<?> notFoundClass) {
    matchOrThrowNotFound(orgId1, orgId2, notFoundClass.getSimpleName());
  }

  public static void matchOrThrowNotFound(OrgId orgId1, OrgId orgId2, String notFoundName) {
    matchOrThrow(orgId1, orgId2, () -> NotFoundException.ofName(notFoundName));
  }
}
