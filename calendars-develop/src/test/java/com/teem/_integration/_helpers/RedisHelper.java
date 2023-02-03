package com.UoU._integration._helpers;

import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthCodeCreateRequest;
import com.UoU.core.auth.AuthCodeRepository;
import com.UoU.core.diagnostics.DiagnosticRepository;
import com.UoU.core.nylas.ExternalEtagRepository;
import java.time.Duration;
import java.util.UUID;
import lombok.Value;
import org.springframework.boot.test.context.TestComponent;

/**
 * Helper for working with redis to setup test data, verify state, etc.
 */
@TestComponent
@Value
public class RedisHelper {
  AuthCodeRepository authCodeRepo;
  DiagnosticRepository diagnosticRepo;
  ExternalEtagRepository externalEtagRepo;

  public UUID createAuthCode(OrgId orgId) {
    return createAuthCode(orgId, Duration.ofSeconds(60));
  }

  public UUID createAuthCode(OrgId orgId, Duration expiration) {
    var code = UUID.randomUUID();
    authCodeRepo.create(new AuthCodeCreateRequest(
        code,
        orgId,
        expiration,
        "https://example.com"));
    return code;
  }
}
