package com.UoU.core.accounts;

import com.UoU.core.OrgId;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.auth.AuthMethod;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface ServiceAccountRepository {
  PagedItems<ServiceAccount> list(OrgId orgId, PageParams page);

  Stream<List<ServiceAccountId>> listExpiredSettings(Set<AuthMethod> authMethods, int batchSize);

  ServiceAccountAccessInfo getAccessInfo(ServiceAccountId id);

  ServiceAccount get(ServiceAccountId id);

  Optional<ServiceAccount> tryGet(String email);

  /**
   * Gets the auth info, including unencrypted settings, so be careful.
   */
  ServiceAccountAuthInfo getAuthInfo(ServiceAccountId id);

  boolean hasAccounts(ServiceAccountId id);

  void create(ServiceAccountCreateRequest request);

  void update(ServiceAccountUpdateRequest request);

  void delete(ServiceAccountId id);
}
