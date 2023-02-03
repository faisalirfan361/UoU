package com.UoU._integration.db;

import static com.UoU._helpers.PagingAssertions.assertPagesContainValues;
import static com.UoU.infra.jooq.Tables.ACCOUNT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.DataConfig;
import com.UoU.core.Fluent;
import com.UoU.core.PageParams;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.AccountCreateRequest;
import com.UoU.core.accounts.AccountError;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthMethod;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class JooqAccountRepositoryTests extends BaseAppIntegrationTest {

  @Test
  void list_shouldPageAndSortByEmail() {
    // Create 3 accounts in the order we expect them returned: by email
    val ids = List.of(
        dbHelper.createAccount(orgId, x -> x.email(TestData.emailStartingWith("a"))),
        dbHelper.createAccount(orgId, x -> x.email(TestData.emailStartingWith("b"))),
        dbHelper.createAccount(orgId, x -> x.email(TestData.emailStartingWith("c"))));

    // Get the 3 items: page1 with 1 item, page2 with 2 items
    val page1 = dbHelper.getAccountRepo().list(orgId, new PageParams(null, 1));
    val page2 = dbHelper.getAccountRepo().list(orgId, new PageParams(page1.nextCursor(), 99));

    assertPagesContainValues(
        x -> x.id(),
        Pair.of(page1, List.of(ids.get(0))),
        Pair.of(page2, List.of(ids.get(1), ids.get(2))));
  }

  @Test
  void list_shouldPageWithEncryptedEmail() {
    val email = TestData.email();
    dbHelper.createAccount(orgId);
    dbHelper.createAccount(orgId, x -> x.email(email));

    val page = dbHelper.getAccountRepo().list(orgId, new PageParams(null, 1));
    val decodedCursor = new String(
        Base64.getUrlDecoder().decode(page.nextCursor()),
        StandardCharsets.UTF_8);

    assertThat(decodedCursor).doesNotContain(email);
  }

  @Test
  void listByAccount_shouldPageAndSortByCreatedAtAndThenEmail() {
    val serviceAccountId = dbHelper.createServiceAccount(orgId);
    val now = OffsetDateTime.now();
    val ids = List.of(
        createSubAccount(serviceAccountId, now, "z"),
        createSubAccount(serviceAccountId, now.plusSeconds(5), "b"),
        createSubAccount(serviceAccountId, now.plusSeconds(5), "c"), //same time as prev
        createSubAccount(serviceAccountId, now.plusSeconds(10), "a")
    );

    //Get the 4 Accounts at 3 per page
    val limit = 3;
    val accountRepo = dbHelper.getAccountRepo();
    val page1 = accountRepo
        .listByServiceAccount(orgId, serviceAccountId, new PageParams(null, limit));
    val page2 = accountRepo.listByServiceAccount(
        orgId, serviceAccountId, new PageParams(page1.nextCursor(), limit));

    assertPagesContainValues(
        x -> x.id(),
        Pair.of(page1, List.of(ids.get(0), ids.get(1), ids.get(2))),
        Pair.of(page2, List.of(ids.get(3))));
  }

  @Test
  void create_getAccessToken_shouldEncryptAndDecryptAccessToken() {
    val id = TestData.accountId();
    val createRequest = AccountCreateRequest.builder()
        .id(id)
        .email(TestData.email())
        .orgId(TestData.orgId())
        .name("Test Account")
        .authMethod(AuthMethod.GOOGLE_OAUTH)
        .accessToken(new SecretString("this-is-a-token"))
        .build();

    dbHelper.getAccountRepo().create(createRequest);
    val accessTokenResult = dbHelper.getAccountRepo().getAccessToken(id);
    val rawAccessTokenResult = Fluent.of(dbHelper
        .getDsl()
        .select(ACCOUNT.ACCESS_TOKEN_ENCRYPTED)
        .from(ACCOUNT)
        .where(ACCOUNT.ID.eq(id.value()))
        .fetchSingle()
        .value1())
        .map(x -> new String(x, StandardCharsets.UTF_8))
        .get();

    assertThat(accessTokenResult.value())
        .as("decrypted value matches input value we sent")
        .isEqualTo(createRequest.accessToken().value());
    assertThat(rawAccessTokenResult)
        .as("raw db value does not contain the unencrypted value")
        .doesNotContain(createRequest.accessToken().value());
  }

  @Test
  void createError_shouldKeepMaxNewestErrors() {
    val id = dbHelper.createAccount(orgId);

    // Create max errors.
    Stream.generate(() -> new AccountError(id, AccountError.Type.AUTH, "msg", "details"))
        .limit(DataConfig.Accounts.MAX_ERRORS_PER_ACCOUNT)
        .forEach(x -> dbHelper.getAccountRepo().createError(x));

    // Then create a newer error that should be kept (older ones should be deleted).
    dbHelper.getAccountRepo().createError(new AccountError(
        UUID.randomUUID(),
        id,
        Instant.now().plusSeconds(900),
        AccountError.Type.AUTH,
        "newest",
        "details"));

    val errors = dbHelper.getAccountRepo().listErrors(id, true).toList();
    assertThat(errors.size()).isEqualTo(DataConfig.Accounts.MAX_ERRORS_PER_ACCOUNT);
    assertThat(errors.stream().filter(x -> x.message().equals("newest")).findFirst()).isPresent();
  }

  private AccountId createSubAccount(
      ServiceAccountId serviceAccountId, OffsetDateTime createdAt, String emailPrefix) {
    val id = TestData.accountId();

    dbHelper.getDsl()
        .newRecord(ACCOUNT)
        .setId(id.value())
        .setCreatedAt(createdAt)
        .setName(id.value())
        .setEmail(TestData.emailStartingWith(emailPrefix))
        .setServiceAccountId(serviceAccountId.value())
        .setAuthMethod(com.UoU.infra.jooq.enums.AuthMethod.google_sa)
        .setOrgId(orgId.value())
        .setAccessTokenEncrypted(TestData.secretString().value().getBytes())
        .setLinkedAt(createdAt)
        .insert();

    return id;
  }
}
