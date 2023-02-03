package com.UoU.core.nylas;

import java.time.Duration;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("nylas.etags")
public record EtagConfig(@NonNull Duration expiration) {
}
