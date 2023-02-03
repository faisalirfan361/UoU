package com.UoU.core.auth;

import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountCreateRequest;
import com.UoU.core.accounts.AccountError;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.AccountUpdateRequest;
import com.UoU.core.accounts.ServiceAccountCreateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.accounts.ServiceAccountUpdateRequest;
import com.UoU.core.auth.serviceaccountsettings.AuthSettingsHandlerProvider;
import com.UoU.core.conferencing.ConferencingUser;
import com.UoU.core.conferencing.ConferencingUserCreateRequest;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.conferencing.ConferencingUserRepository;
import com.UoU.core.conferencing.ConferencingUserUpdateRequest;
import com.UoU.core.nylas.auth.NylasAuthException;
import com.UoU.core.nylas.auth.NylasAuthResult;
import com.UoU.core.nylas.auth.NylasAuthService;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Handles auth flows, including creating service accounts and accounts as a result of auth.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
  private final AuthCodeRepository authCodeRepo;
  private final ServiceAccountRepository serviceAccountRepo;
  private final AccountRepository accountRepo;
  private final ConferencingUserRepository conferencingUserRepo;
  private final ValidatorWrapper validator;
  private final OauthHandlerProvider oauthHandlerProvider;
  private final AuthSettingsHandlerProvider authSettingsHandlerProvider;
  private final NylasAuthService nylasAuthService;
  private final NylasTaskScheduler nylasTaskScheduler;

  public UUID createAuthCode(AuthCodeCreateRequest request) {
    validator.validateAndThrow(request);
    authCodeRepo.create(request);
    return request.code();
  }

  public Optional<AuthCode> tryGetAuthCode(UUID code) {
    return authCodeRepo.tryGet(code);
  }

  public Optional<AuthCode> tryGetValidAuthCode(String code) {
    try {
      return tryGetAuthCode(UUID.fromString(code));
    } catch (IllegalArgumentException ex) {
      return Optional.empty(); // code was not a valid uuid
    }
  }

  public void tryDeleteAuthCode(UUID code) {
    authCodeRepo.tryDelete(code);
  }

  public String getOauthRedirectUrl(AuthMethod method, AuthCode authCode) {
    val state = new OauthState(method, authCode.code());
    return oauthHandlerProvider.provide(method).getRedirectUrl(state);
  }

  /**
   * Handles OAuth auth after the user has been sent through an OAuth authorization code flow.
   *
   * <p>This only works for OAuth authorization code auth methods. For non-OAuth (or 2-legged OAuth)
   * methods, use instead:{@link #handleDirectionSubmissionAuth(AuthMethod, String, Map)}.
   */
  public AuthResult handleOauthCallback(String code, String state) {
    if (code == null || code.isBlank() || state == null || state.isBlank()) {
      // If we get to this point with missing params, it's a coding error, not something user can
      // fix. So throw OAuthException rather than ValidationException.
      throw new OauthException("Missing OAuth code or state");
    }

    // If state cannot be decoded, or does not contain a valid auth code, throw ValidationException
    // because this is probably not our issue, and user should try again.
    val decodedState = OauthState.decode(state)
        .filter(x -> x.authMethod().getFlow() == AuthMethod.Flow.OauthAuthorizationCode)
        .orElseThrow(() -> {
          log.info("OAuth state could not be decoded: {}", state);
          return new ValidationException("Invalid auth state. Please try again.");
        });

    val authCode = tryGetAuthCode(decodedState.authCode())
        .orElseThrow(() -> new ValidationException(
            "Invalid auth code (session may have expired). Please try again."));

    // Delete authCode. If an error occurs after this, a new auth code will be needed.
    tryDeleteAuthCode(authCode.code());

    val method = decodedState.authMethod();
    val oauthHandler = oauthHandlerProvider.provide(method);
    val oauthResult = oauthHandler.handleAuthorizationCode(code);
    if (oauthResult == null) {
      throw new OauthException("OAuth handler result was null");
    }

    val authInput = AuthInput.ofOauthResult(oauthResult);

    return finishAuth(method, authCode, authInput);
  }

  /**
   * Handles auth where the user submits auth credentials directly (e.g. a JSON auth key).
   *
   * <p>This only works for non-OAuth or 2-legged OAuth auth methods. For most OAuth methods,
   * which will use authorization code flow, use: {@link #handleOauthCallback(String, String)}.
   */
  public AuthResult handleDirectionSubmissionAuth(
      AuthMethod method, String authCodeString, Map<String, Object> authData) {

    if (method.getFlow() != AuthMethod.Flow.DirectSubmission) {
      throw new ValidationException(
          "Auth method does not allow user-submitted auth data: " + method.getValue());
    }

    val authCode = tryGetValidAuthCode(authCodeString)
        .orElseThrow(() -> new ValidationException(
            "Invalid auth code (session may have expired). Please try again."));

    val authInput = AuthInput.ofDirectlySubmittedAuthData(authData);
    val result = finishAuth(method, authCode, authInput);

    // Delete authCode only after auth worked since user may want to try submitting data again.
    tryDeleteAuthCode(authCode.code());

    return result;
  }

  private AuthResult finishAuth(AuthMethod method, AuthCode authCode, AuthInput authInput) {
    return switch (method.getDataType()) {
      case CALENDAR -> method.isForServiceAccounts()
          ? finishServiceAccountAuth(method, authCode, authInput)
          : finishAccountAuth(method, authCode, authInput);
      case CONFERENCING -> finishConferencingAuth(method, authCode, authInput);
    };
  }

  private AuthResult finishServiceAccountAuth(
      AuthMethod method, AuthCode authCode, AuthInput authInput) {

    val orgId = authCode.orgId();
    val authSettingsHandler = authSettingsHandlerProvider.provide(method);
    val authSettings = authSettingsHandler.createSettings(authInput);
    val email = authSettings.email();

    val existing = Fluent
        .of(serviceAccountRepo.tryGet(email))
        .ifThenThrow(
            // Email must be globally unique across all orgs. If the user gets to this point,
            // they would have already authed the account, so it's safe to tell the user that the
            // email already exists.
            x -> x.filter(y -> !y.orgId().equals(orgId)).isPresent(),
            () -> new ValidationException("Service account email must be globally unique."))
        .ifThenThrow(
            // If email already exists, we must ensure we're not switching auth methods:
            x -> x.filter(y -> !y.authMethod().equals(method)).isPresent(),
            () -> new ValidationException("Invalid auth method for existing service account."))
        .get();
    val id = existing.map(x -> x.id()).orElseGet(ServiceAccountId::create);

    existing.ifPresentOrElse(
        serviceAccount -> {
          val request = Fluent
              .of(ServiceAccountUpdateRequest.builder()
                  .id(id)
                  .orgId(orgId)
                  .settings(authSettings.settings()))
              .ifThenAlso(authSettings.expiration(), (x, expire) -> x.settingsExpireAt(expire))
              .get()
              .build();
          serviceAccountRepo.update(request);
          nylasTaskScheduler.updateAllSubaccountTokens(id);
        },
        () -> {
          val request = Fluent
              .of(ServiceAccountCreateRequest.builder()
                  .id(id)
                  .orgId(orgId)
                  .email(email)
                  .authMethod(method)
                  .settings(authSettings.settings()))
              .ifThenAlso(authSettings.expiration(), (x, expire) -> x.settingsExpireAt(expire))
              .get()
              .build();
          serviceAccountRepo.create(request);
        });

    return new AuthResult(authCode, id);
  }

  private AuthResult finishAccountAuth(
      AuthMethod method, AuthCode authCode, AuthInput authInput) {

    val oauthResult = authInput.getOauthResult();
    if (oauthResult == null || method.getFlow() != AuthMethod.Flow.OauthAuthorizationCode) {
      // Normal accounts currently only support OAuth auth code methods, and validation on public
      // methods should prevent this exception from happening for real users, but ensure we fail
      // early in case this is called with bad params since OAuth result is expected below here.
      throw new IllegalArgumentException(
          "Normal accounts currently support only OAuth auth code methods.");
    }

    val orgId = authCode.orgId();
    val existingAccount = Fluent
        .of(accountRepo.tryGet(oauthResult.email()))
        .ifThenThrow(
            // Email must be globally unique across all orgs. If the user gets to this point,
            // they would have already authed the account, so it's safe to tell the user that the
            // email already exists.
            x -> x.filter(y -> !y.orgId().equals(orgId)).isPresent(),
            () -> new ValidationException("Account email must be globally unique."))
        .ifThenThrow(
            // If email already exists, ensure we're not switching auth methods:
            x -> x.filter(y -> !y.authMethod().equals(method)).isPresent(),
            () -> new ValidationException("Invalid auth method for existing account."))
        .get();

    val nylasResult = nylasAuthService.authAccount(
        method, oauthResult.refreshToken(), oauthResult.name(), oauthResult.email());

    afterAccountAuth(
        existingAccount, nylasResult, orgId, method, oauthResult.name(), oauthResult.email(), null);

    return new AuthResult(authCode, nylasResult.accountId());
  }

  /**
   * Saves the account and does other common operations after authing an account or subaccount.
   */
  private void afterAccountAuth(
      Optional<Account> existingAccount,
      NylasAuthResult nylasResult,
      OrgId orgId,
      AuthMethod method,
      String name,
      String email,
      @Nullable ServiceAccountId serviceAccountId) {

    existingAccount.ifPresentOrElse(
        account -> {
          if (!account.id().equals(nylasResult.accountId())) {
            // This shouldn't happen, but just in case Nylas does something weird:
            throw new IllegalStateException(String.format(
                "Existing account id %s does not match Nylas account id %s",
                account.id().value(), nylasResult.accountId().value()));
          }

          accountRepo.update(AccountUpdateRequest.builder()
              .id(nylasResult.accountId())
              .name(name)
              .accessToken(nylasResult.accessToken())
              .build());

          // Clear out account errors to start in a clean state on re-auth.
          accountRepo.deleteErrors(nylasResult.accountId(), AccountError.Type.AUTH);
        },
        () -> accountRepo.create(AccountCreateRequest.builder()
            .id(nylasResult.accountId())
            .orgId(orgId)
            .name(name)
            .email(email)
            .serviceAccountId(serviceAccountId)
            .authMethod(method)
            .accessToken(nylasResult.accessToken())
            .build()));

    // Schedule an import of all calendars and events. This task locks inbound sync for the account,
    // so any Nylas webhooks that come in while the sync is running will not be processed.
    // Webhooks for new accounts may take a few minutes to come in though, so we may end up
    // processing some of them after the sync finishes.
    // DO-LATER: We'll need to improve the initial sync to scale better. For an account with many
    // calendars, Nylas could send thousands of webhooks (one per event), which is not an efficient
    // way to sync. We should check with Nylas and see if they'll improve/skip webhooks when an
    // account is initially connected. If we implement delayed task scheduling, we could also
    // use that to wait a bit and then do the full sync because Nylas may not have all the events
    // from the provider when we do this sync right away.
    nylasTaskScheduler.importAllCalendarsFromNylas(nylasResult.accountId(), true);
  }

  private AuthResult finishConferencingAuth(
      AuthMethod method, AuthCode authCode, AuthInput authInput) {

    if (method.isForServiceAccounts()) {
      throw new IllegalArgumentException("Conferencing does not support service accounts.");
    }

    val oauthResult = authInput.getOauthResult();
    if (oauthResult == null || method.getFlow() != AuthMethod.Flow.OauthAuthorizationCode) {
      // Conferencing currently only support OAuth auth code methods, and validation on public
      // methods should prevent this exception from happening for real users, but ensure we fail
      // early in case this is called with bad params since OAuth result is expected below here.
      throw new IllegalArgumentException(
          "Conferencing currently supports only OAuth auth code methods.");
    }

    val existingUser = conferencingUserRepo.tryGet(authCode.orgId(), oauthResult.email());
    val id = existingUser.map(ConferencingUser::id).orElseGet(ConferencingUserId::create);
    val expireAt = Optional
        .ofNullable(oauthResult.expiresIn())
        .map(x -> Instant.now().plusSeconds(x));

    existingUser.ifPresentOrElse(
        user -> conferencingUserRepo.update(ConferencingUserUpdateRequest.builder()
            .id(id)
            .name(oauthResult.name())
            .refreshToken(oauthResult.refreshToken())
            .accessToken(oauthResult.accessToken())
            .expireAt(expireAt.orElse(null))
            .build()),
        () -> conferencingUserRepo.create(ConferencingUserCreateRequest.builder()
            .id(id)
            .orgId(authCode.orgId())
            .email(oauthResult.email())
            .name(oauthResult.name())
            .authMethod(method)
            .refreshToken(oauthResult.refreshToken())
            .accessToken(oauthResult.accessToken())
            .expireAt(expireAt.orElse(null))
            .build()));

    return new AuthResult(authCode, id);
  }

  /**
   * Auths a service account subaccount in Nylas and creates or updates the local account.
   *
   * <p>Account creation is done synchronously, rather than through a scheduled task, because
   * there is no value in creating the account and syncing to Nylas later if Nylas is down. The
   * caller might as well wait and try again later because the account by itself won't do anything.
   * Plus, making it synchronous means we can return the error directly to the caller, which helps
   * troubleshoot common issues like permissions being wrong in the provider.
   */
  public AccountId authSubaccount(SubaccountAuthRequest request) {
    validator.validateAndThrow(request);

    val serviceAccountInfo = serviceAccountRepo.getAuthInfo(request.serviceAccountId());
    serviceAccountInfo.getAccessInfo()
        .requireOrgOrThrowNotFound(request.orgId())
        .requireWritable();

    // If the account already exists, ensure it's connected to the same service account.
    // Otherwise, if the existing account is in the same org, provide a helpful message so the
    // user knows what's wrong. But if the account is in a different org, be vague to not reveal
    // account details the user shouldn't know about.
    val existingAccount = accountRepo.tryGet(request.email());
    val hasInvalidExistingAccount = existingAccount
        .filter(x -> !serviceAccountInfo.id().equals(x.serviceAccountId()))
        .isPresent();
    if (hasInvalidExistingAccount) {
      val isAccountInSameOrg = existingAccount
          .filter(x -> x.orgId().equals(serviceAccountInfo.orgId()))
          .isPresent();
      if (!isAccountInSameOrg) {
        throw new ValidationException("Invalid account email"); // keep this vague
      }

      val existingId = existingAccount.orElseThrow().id().value();
      throw new ValidationException(existingAccount
          .map(x -> x.serviceAccountId())
          .map(x -> String.format(
              "Email is already used for account %s that has a different service account.",
              existingId))
          .orElse(String.format(
              "Email is already used for account %s without a service account.",
              existingId)));
    }

    val nylasResult = nylasAuthService.authSubaccount(
        serviceAccountInfo, request.name(), request.email());

    afterAccountAuth(
        existingAccount, nylasResult, serviceAccountInfo.orgId(), serviceAccountInfo.authMethod(),
        request.name(), request.email(), serviceAccountInfo.id());

    return nylasResult.accountId();
  }

  /**
   * Updates a service account subaccount auth token based on the service account settings.
   *
   * <p>When a service account is re-authed, the settings change, and so associated subaccounts
   * need to have their tokens updated afterward.
   */
  public void updateSubaccountToken(AccountId accountId) {
    val account = accountRepo.get(accountId);
    if (account.serviceAccountId() == null) {
      throw new IllegalArgumentException(
          "Account is not a service account subaccount: " + accountId);
    }

    val serviceAccountInfo = serviceAccountRepo.getAuthInfo(account.serviceAccountId());

    SecretString accessToken;
    try {
      val nylasResult = nylasAuthService.authSubaccount(
          serviceAccountInfo, account.name(), account.email());

      if (!accountId.equals(nylasResult.accountId())) {
        if (!account.id().equals(nylasResult.accountId())) {
          // This shouldn't happen, but just in case Nylas does something weird:
          throw new IllegalStateException(String.format(
              "Existing subaccount id %s does not match Nylas account id %s",
              account.id().value(), nylasResult.accountId().value()));
        }
      }

      accessToken = nylasResult.accessToken();
    } catch (Exception ex) {
      var message = String.format(
          "Account %s auth token could not be updated with service account %s.",
          accountId.value(), account.serviceAccountId().value());

      // NylasAuthExceptions are thrown for specific auth failures and include a message that's
      // safe to show to users, so append the exception message to help troubleshooting.
      if (ex instanceof NylasAuthException) {
        message += " " + ex.getMessage();
      }

      // Unlike the initial auth of the subaccount, this is usually run via an aysnc task with no
      // user present, so save the error so that the user can see it in the account errors list.
      accountRepo.createError(new AccountError(
          accountId,
          AccountError.Type.AUTH,
          message,
          ex.getMessage()));

      throw ex;
    }

    // Set new access token and clear out auth errors because we're in a good state now.
    accountRepo.updateAccessToken(accountId, accessToken);
    accountRepo.deleteErrors(accountId, AccountError.Type.AUTH);
  }

  public void updateServiceAccountRefreshToken(ServiceAccountId serviceAccountId) {
    val serviceAccountInfo = serviceAccountRepo.getAuthInfo(serviceAccountId);
    val authMethod = serviceAccountInfo.authMethod();

    val oauthHandler = oauthHandlerProvider.provide(authMethod);
    if (!oauthHandler.supportsRefresh()) {
      throw new IllegalArgumentException(
          "Service account auth method does not support refresh: " + authMethod.getValue());
    }

    val authSettingsHandler = authSettingsHandlerProvider.provide(authMethod);
    val refreshToken = authSettingsHandler
        .getRefreshTokenFromSettings(serviceAccountInfo.settings())
        .orElseThrow(() -> new IllegalArgumentException(
            "Service account refresh token could not be found in settings: " + serviceAccountId));

    val oauthResult = oauthHandler.refresh(refreshToken);
    if (oauthResult == null) {
      throw new OauthException("OAuth handler result was null");
    }

    val authInput = AuthInput.ofOauthResult(oauthResult);
    val newAuthSettings = authSettingsHandler.createSettings(authInput);

    val updateRequest = Fluent
        .of(ServiceAccountUpdateRequest.builder())
        .map(x -> x
            .id(serviceAccountId)
            .orgId(serviceAccountInfo.orgId())
            .settings(newAuthSettings.settings()))
        .ifThenAlso(newAuthSettings.expiration(), (x, expire) -> x.settingsExpireAt(expire))
        .get()
        .build();
    serviceAccountRepo.update(updateRequest);
  }
}
