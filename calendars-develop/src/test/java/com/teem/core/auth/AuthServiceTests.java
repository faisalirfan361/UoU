package com.UoU.core.auth;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core._helpers.ValidatorWrapperFactory;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountError;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.ServiceAccount;
import com.UoU.core.accounts.ServiceAccountAuthInfo;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.auth.serviceaccountsettings.AuthSettings;
import com.UoU.core.auth.serviceaccountsettings.AuthSettingsHandler;
import com.UoU.core.auth.serviceaccountsettings.AuthSettingsHandlerProvider;
import com.UoU.core.conferencing.ConferencingUser;
import com.UoU.core.conferencing.ConferencingUserRepository;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.auth.NylasAuthException;
import com.UoU.core.nylas.auth.NylasAuthResult;
import com.UoU.core.nylas.auth.NylasAuthService;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.validation.ValidationException;
import lombok.val;
import org.junit.jupiter.api.Test;

class AuthServiceTests {

  @Test
  void tryGetValidAuthCode_shouldWork() {
    val scenario = new Scenario();
    val result = scenario.service.tryGetValidAuthCode(
        scenario.validCode.toString());
    assertThat(result).isPresent();
  }

  @Test
  void tryGetValidAuthCode_shouldReturnEmptyForExpired() {
    val scenario = new Scenario();
    val result = scenario.service.tryGetValidAuthCode(
        scenario.expiredCode.toString());
    assertThat(result).isEmpty();
  }

  @Test
  void tryGetValidAuthCode_shouldReturnEmptyForInvalidUuid() {
    val scenario = new Scenario();
    val result = scenario.service.tryGetValidAuthCode("invalid");
    assertThat(result).isEmpty();
  }

  @Test
  void createAuthCode_shouldCallRepo() {
    val scenario = new Scenario();
    val request = ModelBuilders.authCodeCreateRequestWithTestData().build();

    scenario.service.createAuthCode(request);
    verify(scenario.deps.authCodeRepoMock).create(request);
  }

  @Test
  void createAuthCode_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = ModelBuilders.authCodeCreateRequest()
        .expiration(Duration.ofSeconds(-1)) // negative duration doesn't make sense
        .build();

    assertThatValidationFails(() -> scenario.service.createAuthCode(request));
    verifyNoInteractions(scenario.deps.authCodeRepoMock);
  }

  @Test
  void getOauthRedirectUrl_shouldDelegateToProvider() {
    val scenario = new Scenario();
    val method = AuthMethod.MS_OAUTH_SA;
    val code = ModelBuilders.authCodeWithTestData().build();

    scenario.service.getOauthRedirectUrl(method, code);

    verify(scenario.deps.oauthHandlerMock).getRedirectUrl(argThat(x ->
        x.authCode() == code.code() && x.authMethod() == method));
  }

  @Test
  void handleOauthCallback_shouldDeleteAuthCodeEvenOnProviderException() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, scenario.validCode);
    when(scenario.deps.oauthHandlerMock.handleAuthorizationCode(any()))
        .thenThrow(new RuntimeException("fail"));

    assertThatCode(() -> scenario.service.handleOauthCallback("code", state.encode()))
        .hasMessage("fail");
    verify(scenario.deps.authCodeRepoMock).tryDelete(state.authCode());
  }

  @Test
  void handleOauthCallback_shouldThrowForEmptyCodeOrState() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, scenario.validCode).encode();

    assertThatCode(() -> scenario.service.handleOauthCallback(" ", state))
        .isInstanceOf(OauthException.class)
        .hasMessageContaining("code");

    assertThatCode(() -> scenario.service.handleOauthCallback("code", " "))
        .isInstanceOf(OauthException.class)
        .hasMessageContaining("state");
  }

  @Test
  void handleOauthCallback_shouldThrowWhenProviderReturnsNull() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, scenario.validCode);
    when(scenario.deps.oauthHandlerMock.handleAuthorizationCode(any())).thenReturn(null);

    assertThatCode(() -> scenario.service.handleOauthCallback("code", state.encode()))
        .isInstanceOf(OauthException.class);
  }

  @Test
  void handleOauthCallback_shouldThrowForInvalidAuthMethod() {
    val scenario = new Scenario();
    val state = "invalid-auth-method__" + scenario.validCode;

    assertThatCode(() -> scenario.service.handleOauthCallback("code", state))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("auth state");
  }

  @Test
  void handleOauthCallback__shouldThrowForNonOauthAuthMethod() {
    val scenario = new Scenario();
    val state = AuthMethod.GOOGLE_SA.getValue() + "__" + scenario.validCode;

    assertThatCode(() -> scenario.service.handleOauthCallback("code", state))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("auth state");
  }

  @Test
  void handleOauthCallback_shouldThrowForNonUuidAuthCode() {
    val scenario = new Scenario();
    val state = AuthMethod.MS_OAUTH_SA.getValue() + "__invalid-auth-code";

    assertThatCode(() -> scenario.service.handleOauthCallback("code", state))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("auth state");
  }

  @Test
  void handleOauthCallback_shouldThrowForAuthCodeNotFound() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, UUID.randomUUID());

    assertThatCode(() -> scenario.service.handleOauthCallback("code", state.encode()))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("auth code");
  }

  @Test
  void handleOauthCallback_shouldCreateServiceAccount() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, scenario.validCode);
    val email = TestData.email();
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email));

    val result = scenario.service.handleOauthCallback(oauthCode, state.encode());

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.SERVICE_ACCOUNT);
    assertThat(result.id()).isNotNull();
    verify(scenario.deps.authCodeRepoMock).tryDelete(state.authCode());
    verify(scenario.deps.serviceAccountRepoMock).create(argThat(x -> x.email().equals(email)));
  }

  @Test
  void handleOauthCallback_shouldUpdateServiceAccount() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA);
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, scenario.validCode);
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(
        oauthCode, TestData.oauthResult(scenario.serviceAccount.orElseThrow().email()));

    val result = scenario.service.handleOauthCallback(oauthCode, state.encode());

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.SERVICE_ACCOUNT);
    assertThat(result.id()).isEqualTo(serviceAccountId.value().toString());
    verify(scenario.deps.authCodeRepoMock).tryDelete(state.authCode());
    verify(scenario.deps.serviceAccountRepoMock)
        .update(argThat(x -> x.id().equals(serviceAccountId)));
    verify(scenario.deps.nylasTaskSchedulerMock).updateAllSubaccountTokens(serviceAccountId);
  }

  @Test
  void handleOauthCallback_shouldThrowForServiceAccountEmailInDifferentOrg() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, scenario.validCode);
    val email = TestData.email();
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email));

    // Repo returns service account with same email but different org.
    when(scenario.deps.serviceAccountRepoMock.tryGet(email))
        .thenReturn(Optional.of(ModelBuilders.serviceAccountWithTestData().email(email).build()));

    assertThatCode(() -> scenario.service.handleOauthCallback(oauthCode, state.encode()))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("email");
  }

  @Test
  void handleOauthCallback_shouldCreateAccount() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.GOOGLE_OAUTH, scenario.validCode);
    val email = TestData.email();
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email))
        .withNylasAuthAccountResult(TestData.accountId());

    val result = scenario.service.handleOauthCallback(oauthCode, state.encode());

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.ACCOUNT);
    assertThat(result.id()).isNotNull();
    verify(scenario.deps.authCodeRepoMock).tryDelete(state.authCode());
    verify(scenario.deps.accountRepoMock).create(argThat(x -> x.email().equals(email)));
  }

  @Test
  void handleOauthCallback_shouldUpdateAccountAndDeleteAuthErrors() {
    val scenario = new Scenario().withAccount();
    val accountId = scenario.account.orElseThrow().id();
    val email = scenario.account.orElseThrow().email();
    val state = new OauthState(AuthMethod.GOOGLE_OAUTH, scenario.validCode);
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email))
        .withNylasAuthAccountResult(accountId);

    val result = scenario.service.handleOauthCallback(oauthCode, state.encode());

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.ACCOUNT);
    assertThat(result.id()).isEqualTo(accountId.value());
    verify(scenario.deps.authCodeRepoMock).tryDelete(state.authCode());
    verify(scenario.deps.accountRepoMock).update(argThat(x -> x.id().equals(accountId)));
    verify(scenario.deps.accountRepoMock).deleteErrors(accountId, AccountError.Type.AUTH);
  }

  @Test
  void handleOauthCallback_shouldThrowForAccountEmailInDifferentOrg() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.GOOGLE_OAUTH, scenario.validCode);
    val email = TestData.email();
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email));

    // Repo returns account with same email but different org.
    when(scenario.deps.accountRepoMock.tryGet(email))
        .thenReturn(Optional.of(ModelBuilders.accountWithTestData().email(email).build()));

    assertThatCode(() -> scenario.service.handleOauthCallback(oauthCode, state.encode()))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("email");
  }

  @Test
  void handleOauthCallback_shouldThrowForDifferentNylasAccountId() {
    val scenario = new Scenario().withAccount();
    val email = scenario.account.orElseThrow().email();
    val state = new OauthState(AuthMethod.GOOGLE_OAUTH, scenario.validCode);
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email))
        .withNylasAuthAccountResult(TestData.accountId()); // nylas returns different id

    assertThatCode(() -> scenario.service.handleOauthCallback(oauthCode, state.encode()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Nylas account id");
  }

  @Test
  void handleOauthCallback_shouldCreateConferencingUser() {
    val scenario = new Scenario();
    val state = new OauthState(AuthMethod.CONF_TEAMS_OAUTH, scenario.validCode);
    val email = TestData.email();
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email));

    val result = scenario.service.handleOauthCallback(oauthCode, state.encode());

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.CONFERENCING_USER);
    assertThat(result.id()).isNotNull();
    verify(scenario.deps.authCodeRepoMock).tryDelete(state.authCode());
    verify(scenario.deps.conferencingUserRepoMock).create(argThat(x -> x.email().equals(email)));
  }

  @Test
  void handleOauthCallback_shouldUpdateConferencingUser() {
    val scenario = new Scenario().withConferencingUser();
    val conferencingUserId = scenario.conferencingUser.orElseThrow().id();
    val email = scenario.conferencingUser.orElseThrow().email();
    val state = new OauthState(AuthMethod.CONF_TEAMS_OAUTH, scenario.validCode);
    val oauthCode = TestData.uuidString();
    scenario.withOauthResult(oauthCode, TestData.oauthResult(email));

    val result = scenario.service.handleOauthCallback(oauthCode, state.encode());

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.CONFERENCING_USER);
    assertThat(result.id()).isEqualTo(conferencingUserId.value().toString());
    verify(scenario.deps.authCodeRepoMock).tryDelete(state.authCode());
    verify(scenario.deps.conferencingUserRepoMock).update(
        argThat(x -> x.id().equals(conferencingUserId)));
  }

  @Test
  void handleDirectSubmissionAuth_shouldThrowForInvalidAuthMethod() {
    val scenario = new Scenario();
    val invalidMethod = AuthMethod.MS_OAUTH_SA;

    assertThatCode(() -> scenario.service.handleDirectionSubmissionAuth(
        invalidMethod, scenario.validCode.toString(), Map.of()))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Auth method")
        .hasMessageContaining(invalidMethod.getValue());
  }

  @Test
  void handleDirectSubmissionAuth_shouldThrowForNonUuidAuthCode() {
    val scenario = new Scenario();
    val authCode = "invalid-auth-code";

    assertThatCode(() -> scenario.service.handleDirectionSubmissionAuth(
        AuthMethod.GOOGLE_SA, authCode, Map.of()))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("auth code");
  }

  @Test
  void handleDirectSubmissionAuth_shouldThrowForAuthCodeNotFound() {
    val scenario = new Scenario();
    val authCode = UUID.randomUUID().toString();

    assertThatCode(() -> scenario.service.handleDirectionSubmissionAuth(
        AuthMethod.GOOGLE_SA, authCode, Map.of()))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("auth code");
  }

  @Test
  void handleDirectSubmissionAuth_shouldNotDeleteAuthCodeOnValidationException() {
    val scenario = new Scenario();
    val authCode = "invalid";

    assertThatCode(() -> scenario.service.handleDirectionSubmissionAuth(
        AuthMethod.GOOGLE_SA, authCode, Map.of()))
        .isInstanceOf(ValidationException.class);

    verifyNoInteractions(scenario.deps.authCodeRepoMock);
  }

  @Test
  void handleDirectionSubmissionAuth_shouldCreateServiceAccount() {
    val scenario = new Scenario();
    val email = TestData.email();
    val authData = Map.<String, Object>of("client_email", email);

    val result = scenario.service.handleDirectionSubmissionAuth(
        AuthMethod.GOOGLE_SA,
        scenario.validCode.toString(),
        authData);

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.SERVICE_ACCOUNT);
    assertThat(result.id()).isNotNull();
    verify(scenario.deps.authCodeRepoMock).tryDelete(scenario.validCode);
    verify(scenario.deps.serviceAccountRepoMock).create(argThat(x -> x.email().equals(email)));
  }

  @Test
  void handleDirectionSubmissionAuth_shouldUpdateServiceAccount() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.GOOGLE_SA);
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();
    val email = scenario.serviceAccount.orElseThrow().email();
    val authData = Map.<String, Object>of("client_email", email);

    val result = scenario.service.handleDirectionSubmissionAuth(
        AuthMethod.GOOGLE_SA,
        scenario.validCode.toString(),
        authData);

    assertThat(result.code().code()).isEqualTo(scenario.validCode);
    assertThat(result.idType()).isEqualTo(AuthResult.IdType.SERVICE_ACCOUNT);
    assertThat(result.id()).isEqualTo(serviceAccountId.value().toString());
    verify(scenario.deps.authCodeRepoMock).tryDelete(scenario.validCode);
    verify(scenario.deps.serviceAccountRepoMock)
        .update(argThat(x -> x.id().equals(serviceAccountId)));
    verify(scenario.deps.nylasTaskSchedulerMock).updateAllSubaccountTokens(serviceAccountId);
  }

  @Test
  void handleDirectionSubmissionAuth_shouldThrowForServiceAccountEmailInDifferentOrg() {
    val scenario = new Scenario();
    val email = TestData.email();
    val authData = Map.<String, Object>of("client_email", email);

    // Repo returns service account with same email but different org.
    when(scenario.deps.serviceAccountRepoMock.tryGet(email))
        .thenReturn(Optional.of(ModelBuilders
            .serviceAccountWithTestData()
            .authMethod(AuthMethod.GOOGLE_SA)
            .email(email)
            .build()));

    assertThatCode(() -> scenario.service.handleDirectionSubmissionAuth(
        AuthMethod.GOOGLE_SA, scenario.validCode.toString(), authData))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("email");
  }

  @Test
  void authSubaccount_shouldCreateAccount() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA);
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();
    val request = scenario
        .buildSubaccountAuthRequest()
        .serviceAccountId(serviceAccountId)
        .build();
    scenario.withNylasAuthSubaccountResult(TestData.accountId());

    val resultAccountId = scenario.service.authSubaccount(request);

    assertThat(resultAccountId).isNotNull();
    verify(scenario.deps.accountRepoMock).create(argThat(x -> x.email().equals(request.email())));
  }

  @Test
  void authSubaccount_shouldUpdateAccountAndDeleteAuthErrors() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA).withSubaccount();
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();
    val accountId = scenario.account.orElseThrow().id();
    val accountEmail = scenario.account.orElseThrow().email();
    val request = scenario
        .buildSubaccountAuthRequest()
        .serviceAccountId(serviceAccountId)
        .email(accountEmail)
        .build();
    scenario.withNylasAuthSubaccountResult(accountId);

    val resultAccountId = scenario.service.authSubaccount(request);

    assertThat(resultAccountId).isEqualTo(accountId);
    verify(scenario.deps.accountRepoMock).update(argThat(x -> x.id().equals(accountId)));
    verify(scenario.deps.accountRepoMock).deleteErrors(accountId, AccountError.Type.AUTH);
  }

  @Test
  void authSubaccount_shouldValidateAndFail() {
    val scenario = new Scenario();
    val request = scenario.buildSubaccountAuthRequest()
        .email(null) // invalid
        .build();

    assertThatValidationFails(() -> scenario.service.authSubaccount(request));
  }

  @Test
  void authSubaccount_shouldThrowForServiceAccountInDifferentOrg() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA);
    val request = scenario
        .buildSubaccountAuthRequest()
        .serviceAccountId(scenario.serviceAccount.orElseThrow().id())
        .orgId(TestData.orgId()) // different org
        .build();

    assertThatCode(() -> scenario.service.authSubaccount(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("ServiceAccount");
  }

  @Test
  void authSubaccount_shouldThrowForExisitngAccountWithoutServiceAccount() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA).withSubaccount();
    val accountEmail = scenario.account.orElseThrow().email();
    val request = scenario
        .buildSubaccountAuthRequest()
        .serviceAccountId(scenario.serviceAccount.orElseThrow().id())
        .email(accountEmail)
        .build();

    when(scenario.deps.accountRepoMock.tryGet(accountEmail))
        .thenReturn(Optional.of(ModelBuilders.accountWithTestData()
            .orgId(scenario.orgId)
            .email(accountEmail)
            .serviceAccountId(null) // no service account (normal account)
            .build()));

    assertThatCode(() -> scenario.service.authSubaccount(request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("without a service account");
  }

  @Test
  void authSubaccount_shouldThrowForExistingAccountWithDifferentServiceAccount() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA).withSubaccount();
    val accountEmail = scenario.account.orElseThrow().email();
    val request = scenario
        .buildSubaccountAuthRequest()
        .serviceAccountId(scenario.serviceAccount.orElseThrow().id())
        .email(accountEmail)
        .build();

    when(scenario.deps.accountRepoMock.tryGet(accountEmail))
        .thenReturn(Optional.of(ModelBuilders.accountWithTestData()
            .orgId(scenario.orgId)
            .email(accountEmail)
            .serviceAccountId(TestData.serviceAccountId()) // different service account
            .build()));

    assertThatCode(() -> scenario.service.authSubaccount(request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("different service account");
  }

  @Test
  void authSubaccount_shouldThrowForDifferentNylasAccountId() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA).withSubaccount();
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();
    val accountEmail = scenario.account.orElseThrow().email();
    val request = scenario
        .buildSubaccountAuthRequest()
        .serviceAccountId(serviceAccountId)
        .email(accountEmail)
        .build();
    scenario.withNylasAuthSubaccountResult(TestData.accountId()); // nylas returns different id

    assertThatCode(() -> scenario.service.authSubaccount(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Nylas account id");
  }

  @Test
  void updateSubaccountToken_shouldUpdateAccountTokenAndDeleteAuthErrors() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA).withSubaccount();
    val accountId = scenario.account.orElseThrow().id();
    scenario.withNylasAuthSubaccountResult(accountId);

    scenario.service.updateSubaccountToken(accountId);

    verify(scenario.deps.accountRepoMock).updateAccessToken(eq(accountId), any(SecretString.class));
    verify(scenario.deps.accountRepoMock).deleteErrors(accountId, AccountError.Type.AUTH);
  }

  @Test
  void updateSubaccountToken_shouldThrowIfNotSubaccount() {
    val scenario = new Scenario().withAccount();
    val accountId = scenario.account.orElseThrow().id();

    assertThatCode(() -> scenario.service.updateSubaccountToken(accountId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("subaccount");
  }

  @Test
  void updateSubaccountToken_shouldThrowForDifferentNylasAccountId() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA).withSubaccount();
    val accountId = scenario.account.orElseThrow().id();
    scenario.withNylasAuthSubaccountResult(TestData.accountId()); // nylas returns different id

    assertThatCode(() -> scenario.service.updateSubaccountToken(accountId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Nylas account id");
  }

  @Test
  void updateSubaccountToken_shouldSaveAccountErrorNylasException() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA).withSubaccount();
    val accountId = scenario.account.orElseThrow().id();
    val exception = new NylasAuthException("oops", new IllegalArgumentException());
    when(scenario.deps.nylasAuthServiceMock.authSubaccount(
        any(ServiceAccountAuthInfo.class), anyString(), anyString()))
        .thenThrow(exception);

    assertThatCode(() -> scenario.service.updateSubaccountToken(accountId))
        .isEqualTo(exception);
    verify(scenario.deps.accountRepoMock).createError(argThat(x ->
        x.type().equals(AccountError.Type.AUTH)
        && x.accountId().equals(accountId)
        && x.message().contains(exception.getMessage())));
  }

  @Test
  @SuppressWarnings("unchecked")
  void updateServiceAccountRefreshToken_shouldWork() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA);
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();
    val oldRefreshToken = TestData.secretString();
    val newRefreshToken = TestData.secretString();
    val newOauthResult = new OauthResult(
        "test", "x@y.z", newRefreshToken, TestData.secretString(), 1);
    val newSettings = new AuthSettings("x@y.z", Map.of("test-refresh", newRefreshToken));

    when(scenario.deps.oauthHandlerMock.supportsRefresh()).thenReturn(true);
    when(scenario.deps.oauthHandlerMock.refresh(oldRefreshToken)).thenReturn(newOauthResult);
    when(scenario.deps.authSettingsHandlerMock.getRefreshTokenFromSettings(any(Map.class)))
        .thenReturn(Optional.of(oldRefreshToken));
    when(scenario.deps.authSettingsHandlerMock.createSettings(any(AuthInput.class)))
        .thenReturn(newSettings);

    scenario.service.updateServiceAccountRefreshToken(serviceAccountId);

    verify(scenario.deps.serviceAccountRepoMock).update(argThat(
        x -> x.id().equals(serviceAccountId) && x.settings().equals(newSettings.settings())));
  }

  @Test
  void updateServiceAccountRefreshToken_shouldThrowIfOauthHandlerDoesNotSupportRefresh() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA);
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();

    when(scenario.deps.oauthHandlerMock.supportsRefresh()).thenReturn(false);

    assertThatCode(() -> scenario.service.updateServiceAccountRefreshToken(serviceAccountId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("refresh");
  }

  @Test
  @SuppressWarnings("unchecked")
  void updateServiceAccountRefreshToken_shouldThrowIfOauthSettingsHandlerCantGetRefreshToken() {
    val scenario = new Scenario().withServiceAccount(AuthMethod.MS_OAUTH_SA);
    val serviceAccountId = scenario.serviceAccount.orElseThrow().id();

    when(scenario.deps.oauthHandlerMock.supportsRefresh()).thenReturn(true);
    when(scenario.deps.authSettingsHandlerMock.getRefreshTokenFromSettings(any(Map.class)))
        .thenReturn(Optional.empty());

    assertThatCode(() -> scenario.service.updateServiceAccountRefreshToken(serviceAccountId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("refresh token");
  }

  private static class Scenario {
    private final OrgId orgId = new OrgId(UUID.randomUUID().toString());
    private Optional<ServiceAccount> serviceAccount = Optional.empty();
    private Optional<Account> account = Optional.empty();
    private Optional<ConferencingUser> conferencingUser = Optional.empty();
    private final Dependencies deps = new Dependencies(
        mock(AuthCodeRepository.class),
        mock(ServiceAccountRepository.class),
        mock(AccountRepository.class),
        mock(ConferencingUserRepository.class),
        ValidatorWrapperFactory.createRealInstance(),
        mock(OauthHandlerProvider.class),
        mock(OauthHandler.class),
        mock(AuthSettingsHandlerProvider.class),
        mock(AuthSettingsHandler.class),
        mock(NylasAuthService.class),
        mock(NylasTaskScheduler.class));
    private final AuthService service = new AuthService(
        deps.authCodeRepoMock,
        deps.serviceAccountRepoMock,
        deps.accountRepoMock,
        deps.conferencingUserRepoMock,
        deps.validator,
        deps.oauthHandlerFactoryMock,
        deps.authSettingsHandlerProviderMock,
        deps.nylasAuthServiceMock,
        deps.nylasTaskSchedulerMock);
    private final UUID validCode = UUID.randomUUID();
    private final UUID expiredCode = UUID.randomUUID();

    public Scenario() {
      when(deps.authCodeRepoMock.tryGet(validCode)).thenReturn(
          Optional.of(new AuthCode(validCode, orgId, "https://example.com")));
      when(deps.authCodeRepoMock.tryGet(expiredCode)).thenReturn(Optional.empty());
      when(deps.oauthHandlerFactoryMock.provide(any())).thenReturn(deps.oauthHandlerMock);

      when(deps.authSettingsHandlerProviderMock.provide(any()))
          .thenReturn(deps.authSettingsHandlerMock);
      when(deps.authSettingsHandlerMock.createSettings(any(AuthInput.class)))
          .then(inv -> Optional
              .ofNullable((AuthInput) inv.getArgument(0))
              .flatMap(input -> input.hasOauthResult()
                  ? Optional.of(input.getOauthResult().email())
                  : input.getDirectlySubmittedAuthData().entrySet()
                  .stream()
                  .filter(x -> x.getKey().contains("email"))
                  .map(x -> (String) x.getValue())
                  .findFirst())
              .map(email -> new AuthSettings(email, Map.of()))
              .orElse(null));

      when(deps.serviceAccountRepoMock.tryGet(anyString()))
          .then(inv -> Optional
              .ofNullable((String) inv.getArgument(0))
              .flatMap(email -> serviceAccount.filter(x -> x.email().equals(email))));

      when(deps.serviceAccountRepoMock.getAuthInfo(any(ServiceAccountId.class)))
          .then(inv -> Optional
              .ofNullable((ServiceAccountId) inv.getArgument(0))
              .flatMap(id -> serviceAccount
                  .filter(x -> x.id().equals(id))
                  .map(x -> new ServiceAccountAuthInfo(
                      x.id(), x.orgId(), x.authMethod(), Map.of("some_setting", "test"))))
              .orElseThrow(() -> NotFoundException.ofClass(ServiceAccount.class)));

      when(deps.accountRepoMock.get(any(AccountId.class)))
          .then(inv -> Optional
              .ofNullable((AccountId) inv.getArgument(0))
              .flatMap(id -> account.filter(x -> x.id().equals(id)))
              .orElseThrow(() -> NotFoundException.ofClass(Account.class)));

      when(deps.accountRepoMock.tryGet(anyString()))
          .then(inv -> Optional
              .ofNullable((String) inv.getArgument(0))
              .flatMap(email -> account.filter(x -> x.email().equals(email))));

      when(deps.conferencingUserRepoMock.tryGet(any(OrgId.class), anyString()))
          .then(inv -> Optional
              .ofNullable((OrgId) inv.getArgument(0))
              .flatMap(orgIdArg -> Optional
                  .ofNullable((String) inv.getArgument(1))
                  .flatMap(email -> conferencingUser.filter(
                      x -> x.email().equals(email) && x.orgId().equals(orgIdArg)))));
    }

    public Scenario withServiceAccount(AuthMethod authMethod) {
      serviceAccount = Optional.of(ModelBuilders
          .serviceAccountWithTestData()
          .authMethod(authMethod)
          .orgId(orgId)
          .build());
      return this;
    }

    public Scenario withSubaccount() {
      account = Optional.of(ModelBuilders
          .accountWithTestData()
          .serviceAccountId(serviceAccount.orElseThrow().id())
          .orgId(orgId)
          .build());
      return this;
    }

    public Scenario withAccount() {
      account = Optional.of(ModelBuilders
          .accountWithTestData()
          .orgId(orgId)
          .build());
      return this;
    }

    public Scenario withConferencingUser() {
      conferencingUser = Optional.of(ModelBuilders
          .conferencingUserWithTestData()
          .orgId(orgId)
          .build());
      return this;
    }

    public Scenario withOauthResult(String oauthCode, OauthResult result) {
      when(deps.oauthHandlerMock.handleAuthorizationCode(oauthCode))
          .thenReturn(result);
      return this;
    }

    public Scenario withNylasAuthAccountResult(AccountId accountId) {
      when(deps.nylasAuthServiceMock.authAccount(
          any(AuthMethod.class), any(SecretString.class), anyString(), anyString()))
          .thenReturn(new NylasAuthResult(accountId, new SecretString("test")));
      return this;
    }

    public Scenario withNylasAuthSubaccountResult(AccountId accountId) {
      when(deps.nylasAuthServiceMock.authSubaccount(
          any(ServiceAccountAuthInfo.class), anyString(), anyString()))
          .thenReturn(new NylasAuthResult(accountId, new SecretString("test")));
      return this;
    }

    public ModelBuilders.SubaccountAuthRequestBuilder buildSubaccountAuthRequest() {
      return ModelBuilders.subaccountAuthRequest()
          .orgId(orgId)
          .email(TestData.email())
          .name("Test Account");
    }

    private record Dependencies(
        AuthCodeRepository authCodeRepoMock,
        ServiceAccountRepository serviceAccountRepoMock,
        AccountRepository accountRepoMock,
        ConferencingUserRepository conferencingUserRepoMock,
        ValidatorWrapper validator,
        OauthHandlerProvider oauthHandlerFactoryMock,
        OauthHandler oauthHandlerMock,
        AuthSettingsHandlerProvider authSettingsHandlerProviderMock,
        AuthSettingsHandler authSettingsHandlerMock,
        NylasAuthService nylasAuthServiceMock,
        NylasTaskScheduler nylasTaskSchedulerMock) {
    }
  }
}
