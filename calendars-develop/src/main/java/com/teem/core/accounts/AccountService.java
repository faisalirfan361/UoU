package com.UoU.core.accounts;

import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.OrgMatcher;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountService {
  private final AccountRepository accountRepo;
  private final NylasTaskScheduler nylasTaskScheduler;

  public PagedItems<Account> list(OrgId orgId, PageParams page) {
    return accountRepo.list(orgId, page);
  }

  public Stream<AccountError> listErrors(OrgId orgId, AccountId id, boolean includeDetails) {
    accountRepo.getAccessInfo(id).requireOrgOrThrowNotFound(orgId);
    return accountRepo.listErrors(id, includeDetails);
  }

  public PagedItems<Account> listByServiceAccount(OrgId orgId, ServiceAccountId serviceAccountId,
                                                  PageParams page) {
    return accountRepo.listByServiceAccount(orgId, serviceAccountId, page);
  }

  public Account get(OrgId orgId, AccountId id) {
    return Fluent.of(accountRepo.get(id))
        .also(x -> OrgMatcher.matchOrThrowNotFound(x.orgId(), orgId, Account.class))
        .get();
  }

  public Account get(OrgId orgId, String email) {
    return Fluent.of(accountRepo.get(email))
        .also(x -> OrgMatcher.matchOrThrowNotFound(x.orgId(), orgId, Account.class))
        .get();
  }

  /**
   * In most circumstances this method should not be called. When possible, use the overload that
   * allows for an orgId to be passed. Use this method in rare cases when the org id is not known.
   */
  public Account get(AccountId id) {
    return accountRepo.get(id);
  }

  public void delete(OrgId orgId, AccountId id) {
    accountRepo.getAccessInfo(id)
        .requireOrgOrThrowNotFound(orgId)
        .requireWritable();

    accountRepo.delete(id);
    nylasTaskScheduler.deleteAccountFromNylas(id);
  }

}
