package com.UoU.core.tasks;

import com.UoU.core.Task;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Updates a single service account refresh token.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateServiceAccountRefreshTokenTask
    implements Task<UpdateServiceAccountRefreshTokenTask.Params> {

  private final AuthService authService;

  public record Params(ServiceAccountId serviceAccountId) {
  }

  @Override
  public void run(Params params) {
    authService.updateServiceAccountRefreshToken(params.serviceAccountId());
  }
}
