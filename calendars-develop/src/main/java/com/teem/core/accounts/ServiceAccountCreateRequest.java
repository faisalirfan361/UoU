package com.UoU.core.accounts;

import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.validation.ValidatorChecker;
import com.UoU.core.validation.annotations.Custom;
import java.time.Instant;
import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Custom(use = ServiceAccountCreateRequest.Validator.class)
public record ServiceAccountCreateRequest(
    @Valid @NotNull ServiceAccountId id,
    @Valid @NotNull OrgId orgId,
    @NotEmpty @Email String email,
    @NotNull Map<String, Object> settings,
    Instant settingsExpireAt,
    @NotNull AuthMethod authMethod) {

  @lombok.Builder(builderClassName = "Builder")
  public ServiceAccountCreateRequest {
  }

  public static class Validator implements Custom.Validator<ServiceAccountCreateRequest> {
    private static final ValidatorChecker<ServiceAccountCreateRequest> CHECKER =
        new ValidatorChecker<ServiceAccountCreateRequest>()
            .add((val, ctx) -> val.authMethod() == null
                    || val.authMethod().isForServiceAccounts(),
                "authMethod",
                "Auth method must be a valid method for service accounts.");

    @Override
    public boolean isValid(ServiceAccountCreateRequest value, ConstraintValidatorContext context) {
      return CHECKER.isValid(value, context);
    }
  }
}
