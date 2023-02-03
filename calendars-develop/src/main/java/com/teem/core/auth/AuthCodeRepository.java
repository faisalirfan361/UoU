package com.UoU.core.auth;

import java.util.Optional;
import java.util.UUID;

public interface AuthCodeRepository {
  Optional<AuthCode> tryGet(UUID code);

  void create(AuthCodeCreateRequest request);

  void tryDelete(UUID code);
}
