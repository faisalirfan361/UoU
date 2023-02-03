package com.UoU.core.accounts;

import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import java.util.Map;
import lombok.NonNull;

/**
 * Contains the auth info needed to auth subaccounts, including unnecrypted auth settings.
 *
 * <p>IMPORTANT: This contains sensitive auth settings, so don't log this object and be careful!
 */
public record ServiceAccountAuthInfo(
    @NonNull ServiceAccountId id,
    @NonNull OrgId orgId,
    @NonNull AuthMethod authMethod,
    @NonNull Map<String, Object> settings
) {

  public ServiceAccountAuthInfo {
    if (!authMethod.isForServiceAccounts()) {
      throw new IllegalArgumentException("Invalid auth method for service account: " + authMethod);
    }
  }

  public ServiceAccountAccessInfo getAccessInfo() {
    return new ServiceAccountAccessInfo(orgId);
  }
}
