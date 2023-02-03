package com.UoU.core.nylas.auth;

import com.UoU.core.SecretString;
import com.UoU.core.accounts.AccountId;

public record NylasAuthResult(AccountId accountId, SecretString accessToken) {

}
