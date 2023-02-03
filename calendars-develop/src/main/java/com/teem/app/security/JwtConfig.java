package com.UoU.app.security;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jwt")
record JwtConfig(
    @NonNull String audience,
    @NonNull String publicJwk) {
}
