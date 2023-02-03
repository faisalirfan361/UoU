package com.UoU.core.conferencing;

import com.UoU.core.OrgId;
import java.util.Optional;

public interface ConferencingUserRepository {
  ConferencingUser get(ConferencingUserId id);

  Optional<ConferencingUser> tryGet(OrgId orgId, String email);

  ConferencingAuthInfo getAuthInfo(ConferencingUserId id);

  void create(ConferencingUserCreateRequest request);

  void update(ConferencingUserUpdateRequest request);
}
