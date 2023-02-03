package com.UoU.core.auth;

import com.UoU.core.SecretString;
import lombok.NonNull;

public record OauthResult(
    @NonNull String name,
    @NonNull String email,
    @NonNull SecretString refreshToken,
    @NonNull SecretString accessToken,
    Integer expiresIn
) {
}
