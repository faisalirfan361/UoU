package com.UoU.core.accounts;

import com.UoU.core.OrgId;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.SecretString;
import java.util.Optional;
import java.util.stream.Stream;

public interface AccountRepository {
  PagedItems<Account> list(OrgId orgId, PageParams page);

  Stream<Account> listByServiceAccount(ServiceAccountId serviceAccountId);

  PagedItems<Account> listByServiceAccount(
      OrgId orgId, ServiceAccountId serviceAccountId, PageParams page);

  Stream<AccountError> listErrors(AccountId id, boolean includeDetails);

  Account get(AccountId id);

  Account get(String email);

  Optional<Account> tryGet(String email);

  AccountId getId(String email);

  SecretString getAccessToken(AccountId id);

  AccountAccessInfo getAccessInfo(AccountId id);

  void create(AccountCreateRequest request);

  void createError(AccountError accountError);

  void update(AccountUpdateRequest request);

  void updateAccessToken(AccountId id, SecretString accessToken);

  void updateSyncState(AccountId accountId, SyncState syncState);

  void delete(AccountId id);

  void deleteErrors(AccountId id, AccountError.Type type);

}
