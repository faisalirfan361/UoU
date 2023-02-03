package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.app.v1.dtos.AccountDto;
import com.UoU.app.v1.dtos.AccountErrorDto;
import com.UoU.app.v1.dtos.PageParamsDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.app.v1.mapping.AccountMapper;
import com.UoU.app.v1.mapping.AuthMapper;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountService;
import com.UoU.core.auth.AuthService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Authorize.AccountsWrite // default, but override on each method for clarity
@RestController
@RequestMapping("/v1/accounts")
@AllArgsConstructor
@Slf4j
@Tag(name = "Accounts",
    externalDocs = @ExternalDocumentation(
        description = "See more about accounts and service accounts",
        url = "/docs/v1.html#accounts"))
public class AccountController {
  private static final String SYNC_STATE_DESCRIPTION = "**Sync state:** Account syncState is "
      + "provided directly by Nylas (our 3rd-party sync provider), and it can be useful when "
      + "troubleshooting sync issues. See [Nylas Docs]("
      + "https://developer.nylas.com/docs/the-basics/manage-accounts/account-sync-status/).";

  private final PrincipalProvider principalProvider;
  private final AccountService accountService;
  private final AccountMapper accountMapper;
  private final AuthService authService;
  private final AuthMapper authMapper;

  @Authorize.AccountsRead
  @GetMapping
  @Operation(
      summary = "Get all accounts",
      description = Authorize.AccountsRead.DESCRIPTION + "\n\n" + SYNC_STATE_DESCRIPTION)
  @PageParamsDto.ParametersInQuery
  public PagedItems<AccountDto> list(@Parameter(hidden = true) PageParamsDto page) {
    val pagedItems = accountService.list(
        principalProvider.current().orgId(),
        accountMapper.toPageParamsModel(page));
    return accountMapper.toDto(pagedItems);
  }

  @Authorize.AccountsRead
  @GetMapping("/{id}")
  @Operation(
      summary = "Get account by id",
      description = Authorize.AccountsRead.DESCRIPTION + "\n\n" + SYNC_STATE_DESCRIPTION)
  public AccountDto getById(@PathVariable String id) {
    return accountMapper.toDto(
        accountService.get(principalProvider.current().orgId(), new AccountId(id)));
  }

  @Authorize.AccountsRead
  @GetMapping("/{id}/errors")
  @Operation(
      summary = "Get account errors by account id",
      description = Authorize.AccountsRead.DESCRIPTION
          + "Account errors may exist for an account that is unable to be authorized with the "
          + "calendar provider or that is having other issues. You can check for errors if you are "
          + "seeing issues with an account. For accounts that are working normally, no errors will "
          + "exist.")
  public List<AccountErrorDto> listErrorsByAccountId(@PathVariable String id) {
    // This list is *not* paged because we only keep a small number of errors per account.
    return accountService
        .listErrors(principalProvider.current().orgId(), new AccountId(id), false)
        .map(accountMapper::toDto)
        .toList();
  }

  @Authorize.AccountsWrite
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete account by id",
      description = Authorize.AccountsWrite.DESCRIPTION
          + "**WARNING: This deletes an account and all associated calendars and events from our "
          + "system.** The calendars and events *will not* be deleted from the provider.")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    val accountId = new AccountId(id);
    accountService.delete(principalProvider.current().orgId(), accountId);
    log.debug("Deleted account: {}", accountId);
  }
}
