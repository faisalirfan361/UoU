package com.UoU.core.conferencing.teams;

import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.OauthException;
import com.UoU.core.auth.OauthHandler;
import com.UoU.core.auth.OauthHandlerProvider;
import com.UoU.core.conferencing.ConferencingAuthInfo;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.conferencing.ConferencingUserRepository;
import com.UoU.core.conferencing.ConferencingUserUpdateRequest;
import java.time.Instant;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Auth service for fetching and refreshing Teams OAuth access tokens.
 */
@Service
public class TeamsAuthService {
  private final OauthHandler oauthHandler;
  private final ConferencingUserRepository conferencingUserRepo;

  public TeamsAuthService(
      OauthHandlerProvider oauthHandlerProvider,
      ConferencingUserRepository conferencingUserRepo) {
    this.oauthHandler = oauthHandlerProvider.provide(AuthMethod.CONF_TEAMS_OAUTH);
    this.conferencingUserRepo = conferencingUserRepo;
  }

  /**
   * Gets stored auth info for a user, which may need to be refreshed if expired.
   *
   * <p>This is a wrapper for {@link ConferencingUserRepository#getAuthInfo(ConferencingUserId)}.
   */
  public ConferencingAuthInfo getAuthInfo(ConferencingUserId userId) {
    return conferencingUserRepo.getAuthInfo(userId);
  }

  /**
   * Refreshes the auth info by calling MS to get a new access token, then saves the result.
   */
  public ConferencingAuthInfo refreshAndSaveAuthInfo(
      ConferencingUserId userId, ConferencingAuthInfo authInfo) {

    val oauthResult = oauthHandler.refresh(authInfo.refreshToken());

    // MS should always return expiration, but spec allows null, so just in case:
    if (oauthResult.expiresIn() == null) {
      throw new OauthException("Expected OAuth token expiration");
    }

    val newAuthInfo = new ConferencingAuthInfo(
        oauthResult.name(),
        oauthResult.refreshToken(),
        oauthResult.accessToken(),
        Instant.now().plusSeconds(oauthResult.expiresIn()));

    conferencingUserRepo.update(
        ConferencingUserUpdateRequest.builder()
            .id(userId)
            .name(newAuthInfo.name())
            .refreshToken(newAuthInfo.refreshToken())
            .accessToken(newAuthInfo.accessToken())
            .expireAt(newAuthInfo.expiresAt())
            .build());

    return newAuthInfo;
  }
}
