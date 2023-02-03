package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.app.v1.dtos.AccountDto;
import com.UoU.app.v1.dtos.IdResponse;
import com.UoU.app.v1.dtos.PageParamsDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.app.v1.dtos.ServiceAccountDto;
import com.UoU.app.v1.dtos.SubaccountCreateRequestDto;
import com.UoU.app.v1.mapping.AccountMapper;
import com.UoU.app.v1.mapping.AuthMapper;
import com.UoU.app.v1.mapping.ServiceAccountMapper;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountService;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.ServiceAccountService;
import com.UoU.core.auth.AuthService;
import com.UoU.core.nylas.auth.NylasAuthException;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import javax.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Authorize.AccountsWrite // default, but override on each method for clarity
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/v1/serviceaccounts")
@Tag(name = "Accounts",
    externalDocs = @ExternalDocumentation(
        description = "See more about accounts and service accounts",
        url = "/docs/v1.html#accounts"))
public class ServiceAccountController {
  private final PrincipalProvider principalProvider;
  public final ServiceAccountService serviceAccountService;
  private final ServiceAccountMapper serviceAccountMapper;
  private final AuthService authService;
  private final AuthMapper authMapper;
  private final AccountService accountService;
  private final AccountMapper accountMapper;

  @Authorize.AccountsRead
  @GetMapping
  @Operation(summary = "Get all service accounts", description = Authorize.AccountsRead.DESCRIPTION)
  @PageParamsDto.ParametersInQuery
  public PagedItems<ServiceAccountDto> list(@Parameter(hidden = true) PageParamsDto page) {
    val pagedItems = serviceAccountService.list(
        principalProvider.current().orgId(),
        serviceAccountMapper.toPageParamsModel(page));
    return serviceAccountMapper.toDto(pagedItems);
  }

  @Authorize.AccountsRead
  @GetMapping("/{id}")
  @Operation(
      summary = "Get service account by id",
      description = Authorize.AccountsRead.DESCRIPTION)
  public ServiceAccountDto getById(@PathVariable("id") UUID rawId) {
    val id = new ServiceAccountId(rawId);
    return serviceAccountMapper.toDto(
        serviceAccountService.get(principalProvider.current().orgId(), id));
  }

  @Authorize.AccountsWrite
  @PostMapping("/{id}/accounts")
  @Operation(
      summary = "Create an account associated with the service account and returns the account id",
      description = Authorize.AccountsWrite.DESCRIPTION
          + "Account emails must be unique. If the account email already exists, the account "
          + "will be reauthorized using the service account but keep the same account id.\n\n"
          + "Usually, you don't need to reauthorize existing accounts through this endpoint; "
          + "rather, when a service account's credentials change via the auth endpoints, all the "
          + "associated accounts are automatically reauthorized as well. However, you can use this "
          + "endpoint to change which service account is associated with a child account.")
  public IdResponse<String> create(
      @RequestBody SubaccountCreateRequestDto request, @PathVariable("id") UUID id) {

    AccountId accountId;
    try {
      accountId = authService.authSubaccount(authMapper.toModel(
          request,
          new ServiceAccountId(id),
          principalProvider.current().orgId()));
    } catch (NylasAuthException ex) {
      // Nylas auth errors are usually from bad input and have a message that's safe for users,
      // so translate to ValidationException so this results in 400 like other bad input.
      throw new ValidationException(ex.getMessage(), ex);
    }

    log.debug("Created account {} for service account {}", accountId, id);
    return new IdResponse<>(accountId.value());
  }

  @Authorize.AccountsWrite
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete service account by id",
      description = Authorize.AccountsWrite.DESCRIPTION
          + "All associated accounts must be deleted before you can delete a service account.")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") UUID rawId) {
    val id = new ServiceAccountId(rawId);
    serviceAccountService.delete(principalProvider.current().orgId(), id);
    log.debug("Deleted service account: {}", id);
  }

  @Authorize.AccountsRead
  @GetMapping("/{id}/accounts")
  @Operation(summary = "Get all accounts by service account id",
      description = Authorize.AccountsRead.DESCRIPTION)
  @PageParamsDto.ParametersInQuery
  public PagedItems<AccountDto> listOfAccounts(
      @PathVariable UUID id, @Parameter(hidden = true) PageParamsDto page) {
    var pagedItems = accountService.listByServiceAccount(
        principalProvider.current().orgId(),
        new ServiceAccountId(id),
        accountMapper.toPageParamsModel(page)
    );
    return accountMapper.toDto(pagedItems);
  }
}
