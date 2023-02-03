package com.UoU._fakes;

import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.auth.AuthCodeRepository;
import com.UoU.core.auth.AuthService;
import com.UoU.core.auth.OauthHandlerProvider;
import com.UoU.core.auth.serviceaccountsettings.AuthSettingsHandlerProvider;
import com.UoU.core.conferencing.ConferencingUserRepository;
import com.UoU.core.nylas.auth.NylasAuthService;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;

/**
 * Wrapper to spy on AuthService with mockito, mostly so it's clear and easy to inject.
 */
public class AuthServiceSpy extends AuthService {
  public AuthServiceSpy(
      AuthCodeRepository authCodeRepo,
      ServiceAccountRepository serviceAccountRepo,
      AccountRepository accountRepo,
      ConferencingUserRepository conferencingUserRepo,
      ValidatorWrapper validator,
      OauthHandlerProvider oauthHandlerProvider,
      AuthSettingsHandlerProvider authSettingsHandlerProvider,
      NylasAuthService nylasAuthService,
      NylasTaskScheduler nylasTaskScheduler) {
    super(
        authCodeRepo,
        serviceAccountRepo,
        accountRepo,
        conferencingUserRepo,
        validator,
        oauthHandlerProvider,
        authSettingsHandlerProvider,
        nylasAuthService,
        nylasTaskScheduler);
  }
}
