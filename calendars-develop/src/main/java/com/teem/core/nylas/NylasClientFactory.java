package com.UoU.core.nylas;

import com.nylas.NylasAccount;
import com.nylas.NylasApplication;
import com.nylas.NylasClient;
import com.UoU.core.Fluent;
import com.UoU.core.SecretString;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NylasClientFactory {
  @NonNull private final ClientConfig config;

  public NylasApplication createApplicationClient() {
    return client().application(config.id().value(), config.secret().value());
  }

  public NylasAccount createAccountClient(SecretString accessToken) {
    return client().account(accessToken.value());
  }

  private NylasClient client() {
    return Fluent
        .of(new NylasClient.Builder())
        .ifThenAlso(
            Optional.ofNullable(config.uri()).filter(uri -> !uri.isBlank()),
            (builder, uri) -> builder.baseUrl(uri))
        .get()
        .build();
  }
}
