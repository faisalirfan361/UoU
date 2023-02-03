package com.UoU.core;

import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.exceptions.ReadOnlyException;
import java.util.function.Supplier;

public interface AccessInfo<T extends AccessInfo> {
  String name();

  OrgId orgId();

  boolean isReadOnly();

  default boolean isWritable() {
    return !isReadOnly();
  }

  default boolean isOrg(OrgId otherOrgId) {
    return orgId().value().equals(otherOrgId.value());
  }

  default T requireOrgOrThrow(OrgId requiredOrgId, Supplier<RuntimeException> throwable) {
    OrgMatcher.matchOrThrow(orgId(), requiredOrgId, throwable);
    return self();
  }

  default T requireOrgOrThrowNotFound(OrgId requiredOrgId) {
    return requireOrgOrThrow(requiredOrgId, () -> NotFoundException.ofName(name()));
  }

  default T requireWritable() {
    if (isReadOnly()) {
      throw new ReadOnlyException(name() + " is read-only");
    }
    return self();
  }

  private T self() {
    @SuppressWarnings("unchecked") var self = (T) this;
    return self;
  }
}
