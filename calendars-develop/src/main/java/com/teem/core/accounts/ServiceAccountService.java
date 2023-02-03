package com.UoU.core.accounts;

import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.OrgMatcher;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.exceptions.IllegalOperationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServiceAccountService {
  private final ServiceAccountRepository repo;

  public PagedItems<ServiceAccount> list(OrgId orgId, PageParams page) {
    return repo.list(orgId, page);
  }

  public ServiceAccount get(OrgId orgId, ServiceAccountId id) {
    return Fluent.of(repo.get(id))
        .also(x -> OrgMatcher.matchOrThrowNotFound(x.orgId(), orgId, ServiceAccount.class))
        .get();
  }

  public void delete(OrgId orgId, ServiceAccountId id) {
    repo.getAccessInfo(id)
        .requireOrgOrThrowNotFound(orgId);

    if (repo.hasAccounts(id)) {
      throw new IllegalOperationException(
          "Service account cannot be deleted until the associated accounts are deleted.");
    }

    repo.delete(id);
  }
}
