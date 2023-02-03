package com.UoU.core.auth;

import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.conferencing.ConferencingUserId;

/**
 * The result of successful auth.
 *
 * @param code The auth code that was used for the auth process.
 * @param idType The type of identifier for the successful auth.
 * @param id The identifier value of idType.
 */
public record AuthResult(AuthCode code, IdType idType, String id) {

  public AuthResult(AuthCode code, AccountId accountId) {
    this(code, IdType.ACCOUNT, accountId.value());
  }

  public AuthResult(AuthCode code, ServiceAccountId serviceAccountId) {
    this(code, IdType.SERVICE_ACCOUNT, serviceAccountId.value().toString());
  }

  public AuthResult(AuthCode code, ConferencingUserId conferencingUserId) {
    this(code, IdType.CONFERENCING_USER, conferencingUserId.value().toString());
  }

  public enum IdType { ACCOUNT, SERVICE_ACCOUNT, CONFERENCING_USER }
}
