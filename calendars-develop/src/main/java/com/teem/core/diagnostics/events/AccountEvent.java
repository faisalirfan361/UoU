package com.UoU.core.diagnostics.events;

import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountError;
import com.UoU.core.accounts.AccountId;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * Diagnostics events related to accounts and auth.
 */
public interface AccountEvent extends DiagnosticEvent {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class AccountErrorsChecked extends BaseEvent implements AccountEvent {
    private static final String MSG_NO_ERRORS = "No account errors were found.";
    private static final String MSG_ERRORS = "Account errors were found, which may indicate "
        + "calendar sync won't work properly. Fetch all account errors for more details.";

    public AccountErrorsChecked(AccountId accountId) {
      super(MSG_NO_ERRORS, Map.of(
          "accountId", accountId.value(),
          "hasErrors", false));
    }

    public AccountErrorsChecked(AccountId accountId, int errorCount, AccountError firstError) {
      super(MSG_ERRORS, Map.of(
          "accountId", accountId.value(),
          "hasErrors", true,
          "errorCount", errorCount,
          "firstError", firstError.message()));
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class AccountFetchedExternal extends BaseEvent implements AccountEvent {
    public AccountFetchedExternal(com.nylas.Account nylasAccount) {
      super(
          "Account info was fetched from external provider.",
          Map.of("account", Map.of(
              "id", nylasAccount.getAccountId(),
              "email", nylasAccount.getEmail(),
              "provider", getProviderDisplay(nylasAccount),
              "syncState", nylasAccount.getSyncState())));
    }

    private static String getProviderDisplay(com.nylas.Account nylasAccount) {
      val provider = nylasAccount.getProvider().toLowerCase(Locale.ROOT);
      return switch (provider) {
        case "ews", "graph", "exchange", "office365" -> "Microsoft (" + provider + ")";
        case "gmail" -> "Google (" + provider + ")";
        default -> provider;
      };
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class AccountAuthVerified extends BaseEvent implements AccountEvent {
    public AccountAuthVerified(Account account) {
      super(
          "Account auth was verified.",
          Map.of("account", Map.of(
              "id", account.id().value(),
              "email", account.email(),
              "serviceAccountId", Optional
                  .ofNullable(account.serviceAccountId())
                  .map(x -> x.value().toString())
                  .orElse(""))));
    }
  }
}
